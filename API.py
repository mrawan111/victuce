from flask import Flask, request, jsonify
import psycopg2
import os
from flask import Flask
from flask_cors import CORS
import logging
import sys
from functools import wraps
import jwt
from datetime import datetime, timedelta
from marshmallow import Schema, fields, validate, ValidationError
from flask_caching import Cache
from flask_swagger_ui import get_swaggerui_blueprint
from werkzeug.security import generate_password_hash, check_password_hash



# Configure logging
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    stream=sys.stdout
)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

# Add JWT Secret Key
app.config['SECRET_KEY'] = os.getenv('JWT_SECRET_KEY', 'your-secret-key')



# Configure caching
cache = Cache(app, config={
    'CACHE_TYPE': 'simple',  # Use Redis in production
    'CACHE_DEFAULT_TIMEOUT': 300
})

# Configure Swagger
SWAGGER_URL = '/api/docs'
API_URL = '/static/swagger.json'

swaggerui_blueprint = get_swaggerui_blueprint(
    SWAGGER_URL,
    API_URL,
    config={
        'app_name': "Victus Store API"
    }
)

app.register_blueprint(swaggerui_blueprint, url_prefix=SWAGGER_URL)

def get_db_connection():
    """Get a connection to the PostgreSQL database"""
    try:
        conn = psycopg2.connect(
            dbname="Victus_store",
            user="postgres",
            password="aminhismamin",
            host="database-1.cvgye8qeuuef.eu-north-1.rds.amazonaws.com",
            port="5432"
        )
        return conn
    except Exception as e:
        logger.error(f"Database connection error: {str(e)}")
        raise

# Add validation schemas
class AccountSchema(Schema):
    email = fields.Email(required=True)
    password = fields.Str(required=True, validate=validate.Length(min=8))
    phone_num = fields.Str(validate=validate.Length(equal=10))
    seller_account = fields.Boolean(default=False)
    first_name = fields.Str(required=True)
    last_name = fields.Str(required=True)

class ProductSchema(Schema):
    product_name = fields.Str(required=True)
    description = fields.Str(required=True)
    base_price = fields.Float(required=True, validate=validate.Range(min=0))
    category_id = fields.Int(required=True)
    seller_id = fields.Int(required=True)
    variants = fields.List(fields.Dict())

def validate_request(schema_class):
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            schema = schema_class()
            try:
                validated_data = schema.load(request.json)
            except ValidationError as err:
                raise APIError('Validation error', payload=err.messages)
            return f(*args, **kwargs)
        return decorated_function
    return decorator

# JWT token verification decorator
def token_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('Authorization')
        if not token:
            return jsonify({'message': 'Token is missing'}), 401
        try:
            token = token.split(' ')[1]  # Remove 'Bearer ' prefix
            data = jwt.decode(token, app.config['SECRET_KEY'], algorithms=["HS256"])
            current_user = data['email']
        except:
            return jsonify({'message': 'Token is invalid'}), 401
        return f(current_user, *args, **kwargs)
    return decorated

# Login endpoint
@app.route('/login', methods=['POST'])
def login():
    auth = request.json
    if not auth or not auth.get('email') or not auth.get('password'):
        return jsonify({'message': 'Missing credentials'}), 401

    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    
    try:
        cursor.execute("SELECT email, password FROM Accounts WHERE email = %s", (auth.get('email'),))
        user = cursor.fetchone()

        if not user:
            return jsonify({'message': 'Account not found'}), 401
        
        if not check_password_hash(user[1], auth.get('password')):  # Use check_password_hash for password comparison
            return jsonify({'message': 'Invalid password'}), 401

        token = jwt.encode({
            'email': user[0],
            'exp': datetime.utcnow() + timedelta(hours=24)
        }, app.config['SECRET_KEY'])

        return jsonify({'token': token})
    except Exception as e:
        logger.error(f"Login error: {e}")
        return jsonify({'message': 'Internal server error'}), 500
    finally:
        cursor.close()
        cnxn.close()

@app.route('/')
def home():
    return jsonify({
        "status": "running",
        "message": "API is running. Available endpoints can be found at /routes"
    }), 200

# Wrap the original get_db_connection with error handling
def safe_db_connect(func):
    def wrapper(*args, **kwargs):
        conn = get_db_connection()
        if conn is None:
            return jsonify({
                "error": "Database connection failed",
                "message": "Unable to connect to the database. Please try again later."
            }), 503
        try:
            return func(*args, **kwargs)
        except Exception as e:
            logger.error(f"Unexpected error in {func.__name__}: {e}")
            return jsonify({
                "error": "Server error",
                "message": "An unexpected error occurred"
            }), 500
    wrapper.__name__ = func.__name__
    return wrapper

def dictify_row(row, cursor):
    """Convert a sqlite3 row to a dictionary."""
    if isinstance(row, dict):
        return row
    columns = [desc[0] for desc in cursor.description]
    return dict(zip(columns, row))

# API endpoint to create a new account
@app.route('/accounts', methods=['POST'])
@validate_request(AccountSchema)
def create_account():
    cnxn = get_db_connection()
    if not cnxn:
        return jsonify({'error': 'Database connection failed'}), 503
        
    cursor = cnxn.cursor()
    data = request.json
    email = data['email']
    password = data['password']
    phone_num = data.get('phone_num')  # Made optional
    seller_account = data.get('seller_account', False)  # Default to False
    first_name = data['first_name']
    last_name = data['last_name']

    try:
        # Start transaction
        cursor.execute("BEGIN")
        
        try:
            # Check if email already exists
            check_query = "SELECT * FROM Accounts WHERE email = %s"
            cursor.execute(check_query, (email,))
            existing_account = cursor.fetchone()

            if existing_account:
                cursor.execute("ROLLBACK")
                return jsonify({'error': 'Email already registered'}), 400

            # Insert new account
            query = """
            INSERT INTO Accounts (email, password, phone_num, seller_account, first_name, last_name) 
            VALUES (%s, %s, %s, %s, %s, %s)
            """
            cursor.execute(query, (email, generate_password_hash(password), phone_num, seller_account, first_name, last_name))

            # If this is a seller account, create a seller record
            seller_id = None
            if seller_account:
                seller_query = """
                INSERT INTO Sellers (email, seller_name) 
                VALUES (%s, %s)
                RETURNING seller_id
                """
                cursor.execute(seller_query, (email, f"{first_name} {last_name}"))
                seller_id = cursor.fetchone()[0]

            # Commit transaction
            cursor.execute("COMMIT")
            
            response = {
                'message': 'Account created successfully',
                'email': email
            }
            if seller_id:
                response['seller_id'] = seller_id
                
            return jsonify(response), 201

        except Exception as e:
            cursor.execute("ROLLBACK")
            logger.error(f"Transaction error during account creation: {e}")
            return jsonify({'error': 'Failed to create account'}), 500

    except Exception as e:
        logger.error(f"Account creation error: {e}")
        return jsonify({'error': 'Internal server error'}), 500
    finally:
        cursor.close()
        cnxn.close()

@app.route('/accounts', methods=['GET'])
def get_accounts():
    """Get all accounts"""
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        
        cursor.execute("""
            SELECT email, first_name, last_name, phone_num, seller_account, created_at
            FROM Accounts
            ORDER BY created_at DESC
        """)
        
        accounts = []
        for row in cursor.fetchall():
            account = {
                'email': row[0],
                'first_name': row[1],
                'last_name': row[2],
                'phone_num': row[3],
                'seller_account': bool(row[4]),
                'created_at': row[5]
            }
            accounts.append(account)
        
        cursor.close()
        cnxn.close()
        return jsonify(accounts), 200
    except Exception as e:
        logger.error(f"Unexpected error in get_accounts: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/accounts/<email>', methods=['GET'])
def get_account(email):
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        query = "SELECT * FROM Accounts WHERE email = %s"
        
        cursor.execute(query, (email,))
        row = cursor.fetchone()

        if row:
            account = {
                'email': row[0],
                'password': row[1],
                'phone_num': row[2],
                'seller_account': row[3],
                'first_name': row[4],
                'last_name': row[5]
            }
            cursor.close()
            cnxn.close()
            return jsonify(account), 200
        else:
            cursor.close()
            cnxn.close()
            return jsonify({'message': 'Account not found'}), 404
    except Exception as e:
        logger.error(f"Error in get_account: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/accounts/update', methods=['PUT'])
def update_account():
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json

    email = data.get('email')
    if not email:
        return jsonify({'error': 'Email is required'}), 400

    phone_num = data.get('phone_num')
    first_name = data.get('first_name')
    last_name = data.get('last_name')

    update_fields = []
    params = []

    if phone_num:
        update_fields.append("phone_num = %s")
        params.append(phone_num)
    if first_name:
        update_fields.append("first_name = %s")
        params.append(first_name)
    if last_name:
        update_fields.append("last_name = %s")
        params.append(last_name)

    if not update_fields:
        return jsonify({'error': 'No data provided for update'}), 400

    params.append(email)
    query = f"UPDATE Accounts SET {', '.join(update_fields)} WHERE email = %s"

    cursor.execute(query, tuple(params))
    cnxn.commit()

    print(f"✅ Account updated for {email} with values: {data}")  # طباعة البيانات لتأكيد التحديث

    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Account updated successfully'}), 200


# API endpoint to delete an account
@app.route('/accounts/<string:email>', methods=['DELETE'])
def delete_account(email):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    
    try:
        # Start transaction
        cursor.execute("BEGIN")
        
        # Check if account exists
        cursor.execute("SELECT email FROM Accounts WHERE email = %s", (email,))
        if not cursor.fetchone():
            cursor.execute("ROLLBACK")
            return jsonify({'error': 'Account not found'}), 404

        # Check if account is already in deleted_accounts
        cursor.execute("SELECT email FROM Deleted_Accounts WHERE email = %s", (email,))
        if cursor.fetchone():
            # Account is already deleted, just delete from Accounts
            cursor.execute("DELETE FROM Accounts WHERE email = %s", (email,))
        else:
            # Add to deleted_accounts and delete from Accounts
            cursor.execute("""
                INSERT INTO Deleted_Accounts (email, deleted_at)
                VALUES (%s, CURRENT_TIMESTAMP)
            """, (email,))
            cursor.execute("DELETE FROM Accounts WHERE email = %s", (email,))
            
        # Commit transaction
        cursor.execute("COMMIT")
        return jsonify({'message': 'Account deleted successfully'}), 200
            
    except Exception as e:
        cursor.execute("ROLLBACK")
        logging.error(f"Error in delete_account: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500
    finally:
        cursor.close()
        cnxn.close()

# API endpoint to create a new seller
@app.route('/sellers', methods=['POST'])
def create_seller():
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json
    seller_name = data['seller_name']
    email = data['email']

    # Check if the email already exists
    email_query = "SELECT * FROM Sellers WHERE email = %s"
    cursor.execute(email_query, (email,))
    existing_email = cursor.fetchone()

    if existing_email:
        cursor.close()
        cnxn.close()
        return jsonify({'message': 'This email is already associated with a seller.'}), 400

    # Check if the seller name already exists
    name_query = "SELECT * FROM Sellers WHERE seller_name = %s"
    cursor.execute(name_query, (seller_name,))
    existing_name = cursor.fetchone()

    if existing_name:
        cursor.close()
        cnxn.close()
        return jsonify({'message': 'This seller name is already used.'}), 400

    # If both checks pass, insert the new seller
    query = "INSERT INTO Sellers (seller_name, email) VALUES (%s, %s)"
    cursor.execute(query, (seller_name, email))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Seller created successfully'}), 201

# API endpoint to get all sellers
@app.route('/sellers', methods=['GET'])
def get_sellers():
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        query = "SELECT * FROM Sellers"
        cursor.execute(query)
        rows = cursor.fetchall()

        sellers = []
        for row in rows:
            seller = {
                'seller_id': row[0],
                'seller_name': row[1],
                'email': row[2]
            }
            sellers.append(seller)

        cursor.close()
        cnxn.close()
        return jsonify(sellers), 200
    except Exception as e:
        logger.error(f"Error in get_sellers: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

# API endpoint to get a seller by id
@app.route('/sellers/<seller_id>', methods=['GET'])
def get_seller(seller_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "SELECT * FROM Sellers WHERE seller_id = %s"
    cursor.execute(query, (seller_id,))
    row = cursor.fetchone()

    if row:
        seller = {
            'seller_id': row[0],
            'seller_name': row[1],
            'email': row[2]
        }
        cursor.close()
        cnxn.close()
        return jsonify(seller), 200
    else:
        cursor.close()
        cnxn.close()
        return jsonify({'message': 'Seller not found'}), 404

# API endpoint to update a seller
@app.route('/sellers/<seller_id>', methods=['PUT'])
def update_seller(seller_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json
    seller_name = data['seller_name']
    email = data['email']
    query = "UPDATE Sellers SET seller_name = %s, email = %s WHERE seller_id = %s"
    cursor.execute(query, (seller_name, email, seller_id))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Seller updated successfully'}), 200

# API endpoint to delete a seller
@app.route('/sellers/<seller_id>', methods=['DELETE'])
def delete_seller(seller_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "DELETE FROM Sellers WHERE seller_id = %s"
    cursor.execute(query, (seller_id,))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Seller deleted successfully'}), 200

@app.route('/check_account/<string:email>/<string:password>', methods=['GET'])
def check_account(email, password):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    # Step 1: Check if email exists
    email_query = "SELECT password, seller_account FROM Accounts WHERE email = %s"
    cursor.execute(email_query, (email,))
    row = cursor.fetchone()

    if row:
        stored_password = row[0]  # Get the stored password
        is_seller = row[1]  # Get the seller account flag (0 or 1)

        # Step 2: Check if the provided password matches the stored one
        if check_password_hash(stored_password, password):
            cursor.close()
            cnxn.close()
            return jsonify({'password': True, 'exists': True, 'is_seller': bool(is_seller)}), 200
        else:
            cursor.close()
            cnxn.close()
            return jsonify({'password': False, 'exists': True}), 401
    else:
        cursor.close()
        cnxn.close()
        return jsonify({'exists': False}), 404

# API endpoint to create a new category
@app.route('/categories', methods=['POST'])
def create_category():
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json

    try:
        if not data or 'category_name' not in data:
            return jsonify({'error': 'Missing required field: category_name'}), 400

        category_name = data['category_name']
        category_image = data.get('category_image')

        # Generate a unique category name if it already exists
        base_name = category_name
        counter = 1
        while True:
            cursor.execute("SELECT category_id FROM Categories WHERE category_name = %s", (category_name,))
            if not cursor.fetchone():
                break
            category_name = f"{base_name} {counter}"
            counter += 1

        query = "INSERT INTO Categories (category_name, category_image) VALUES (%s, %s) RETURNING category_id"
        cursor.execute(query, (category_name, category_image))
        category_id = cursor.fetchone()[0]
        cnxn.commit()
        
        return jsonify({
            'message': 'Category created successfully',
            'category_id': category_id,
            'category_name': category_name
        }), 201

    except Exception as e:
        cnxn.rollback()
        logger.error(f"Category creation error: {e}")
        return jsonify({'error': 'Failed to create category'}), 500
    finally:
        cursor.close()
        cnxn.close()

@app.route('/categories', methods=['GET'])
@cache.cached(timeout=300)  # Cache for 5 minutes
def get_categories():
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        query = "SELECT * FROM Categories"
        cursor.execute(query)
        rows = cursor.fetchall()

        categories = []
        for row in rows:
            category = {
                'category_id': row[0],
                'category_name': row[1],
                'category_image': row[2]
            }
            categories.append(category)

        cursor.close()
        cnxn.close()
        return jsonify(categories), 200
    except Exception as e:
        logger.error(f"Error in get_categories: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

# API endpoint to get a category by id
@app.route('/categories/<category_id>', methods=['GET'])
def get_category(category_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "SELECT * FROM Categories WHERE category_id = %s"
    cursor.execute(query, (category_id,))
    row = cursor.fetchone()

    if row:
        category = {
            'category_id': row[0],
            'category_name': row[1],
            'category_image': row[2]
        }
        cursor.close()
        cnxn.close()
        return jsonify(category), 200
    else:
        cursor.close()
        cnxn.close()
        return jsonify({'message': 'Category not found'}), 404

# API endpoint to get all products of a specific category
@app.route('/categories/<int:category_id>/products', methods=['GET'])
def get_products_by_category(category_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    
    try:
        query = """
        SELECT 
            p.product_id, p.product_name, p.description, p.base_price, p.category_id, p.seller_id, i.image_url,
            v.variant_id, v.color, v.size, v.stock_quantity, v.price as variant_price
        FROM Products p
        LEFT JOIN Images i ON p.product_id = i.product_id
        LEFT JOIN Product_Variants v ON p.product_id = v.product_id
        WHERE p.category_id = %s
        """
        cursor.execute(query, (category_id,))  # ✅ Corrected parameter passing
        rows = cursor.fetchall()

        if not rows:
            return jsonify({'message': 'No products found for this category'}), 404

        products = {}
        for row in rows:
            product_id = row[0]
            if product_id not in products:
                products[product_id] = {
                    'product_id': row[0],
                    'product_name': row[1],
                    'description': row[2],
                    'base_price': row[3],  # Base price from Products table
                    'category_id': row[4],
                    'seller_id': row[5],
                    'image_url': row[6],
                    'variants': []
                }
            if row[7]:  # Ensure there are variants
                products[product_id]['variants'].append({
                    'variant_id': row[7],
                    'color': row[8],
                    'size': row[9],
                    'stock_quantity': row[10],
                    'price': row[11]  # Price from Product_Variants
                })

        cursor.close()
        cnxn.close()
        return jsonify(list(products.values())), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500

# API endpoint to update a category
@app.route('/categories/<category_id>', methods=['PUT'])
def update_category(category_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()

    try:
        data = request.get_json()
        if not data or 'category_name' not in data:
            return jsonify({'error': 'Category name is required'}), 400

        category_name = data['category_name']
        category_image = data.get('category_image')

        # Start transaction
        cursor.execute("BEGIN")

        # Check if category exists
        cursor.execute("SELECT category_name FROM Categories WHERE category_id = %s", (category_id,))
        result = cursor.fetchone()
        if not result:
            cursor.execute("ROLLBACK")
            return jsonify({'error': 'Category not found'}), 404

        # Check if new category name already exists (excluding current category)
        cursor.execute("""
            SELECT category_id 
            FROM Categories 
            WHERE category_name = %s AND category_id != %s
        """, (category_name, category_id))
        if cursor.fetchone():
            cursor.execute("ROLLBACK")
            return jsonify({'error': 'Category name already exists'}), 400

        # Update category
        if category_image:
            cursor.execute("""
                UPDATE Categories 
                SET category_name = %s, category_image = %s 
                WHERE category_id = %s
            """, (category_name, category_image, category_id))
        else:
            cursor.execute("""
                UPDATE Categories 
                SET category_name = %s, 
                    category_image = %s 
                WHERE category_id = %s
            """, (category_name, category_image, category_id))

        cursor.execute("COMMIT")
        return jsonify({'message': 'Category updated successfully'}), 200

    except Exception as e:
        cursor.execute("ROLLBACK")
        logging.error(f"Error in update_category: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500
    finally:
        cursor.close()
        cnxn.close()

# API endpoint to delete a category
@app.route('/categories/<category_id>', methods=['DELETE'])
def delete_category(category_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "DELETE FROM Categories WHERE category_id = %s"
    cursor.execute(query, (category_id,))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Category deleted successfully'}), 200
@app.route('/products', methods=['POST'])
def create_product():
    """Create a new product"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({'error': 'No data provided'}), 400
            
        required_fields = ['product_name', 'base_price', 'category_id', 'seller_id']
        for field in required_fields:
            if field not in data:
                return jsonify({'error': f'Missing required field: {field}'}), 400
        
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        
        # Check if seller exists
        cursor.execute("SELECT 1 FROM Sellers WHERE seller_id = %s", (data['seller_id'],))
        if not cursor.fetchone():
            return jsonify({'error': 'Invalid seller_id'}), 400
            
        # Check if category exists
        cursor.execute("SELECT 1 FROM Categories WHERE category_id = %s", (data['category_id'],))
        if not cursor.fetchone():
            return jsonify({'error': 'Invalid category_id'}), 400
        
        # Insert product and return its ID
        cursor.execute("""
            INSERT INTO Products (product_name, description, base_price, category_id, seller_id)
            VALUES (%s, %s, %s, %s, %s) RETURNING product_id
        """, (
            data['product_name'],
            data.get('description', ''),
            data['base_price'],
            data['category_id'],
            data['seller_id']
        ))

        product = cursor.fetchone()
        if product is None:
            cnxn.rollback()
            return jsonify({'error': 'Failed to insert product'}), 500
        
        product_id = product[0]
        print(f"✅ Product ID: {product_id}")  # Debugging

        # Insert variants if provided
        if 'variants' in data:
            for variant in data['variants']:
                cursor.execute("""
                    INSERT INTO Product_Variants (product_id, color, size, stock_quantity, price)
                    VALUES (%s, %s, %s, %s, %s)
                """, (
                    product_id,
                    variant.get('color', ''),
                    variant.get('size', ''),
                    variant.get('stock_quantity', 0),
                    variant.get('price', data['base_price'])
                ))
                print(f"✅ Inserted Variant for Product ID: {product_id}")  # Debugging
        
        cnxn.commit()
        cursor.close()
        cnxn.close()
        
        return jsonify({
            'message': 'Product created successfully',
            'product_id': product_id
        }), 201
        
    except Exception as e:
        logger.error(f"Error creating product: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/products', methods=['GET'])
def get_products():
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        query = """
        SELECT 
            p.product_id, p.product_name, p.description, p.base_price, 
            p.category_id, p.seller_id, p.product_rating, p.is_active,
            i.image_url,
            v.variant_id, v.color, v.size, v.stock_quantity, v.price as variant_price,
            v.sku, v.is_active as variant_active
        FROM Products p
        LEFT JOIN Images i ON p.product_id = i.product_id AND i.is_primary = true
        LEFT JOIN Product_Variants v ON p.product_id = v.product_id
        WHERE p.is_active = true
        """
        cursor.execute(query)
        rows = cursor.fetchall()

        products = {}
        for row in rows:
            product_id = row[0]
            if product_id not in products:
                products[product_id] = {
                    'product_id': row[0],
                    'product_name': row[1],
                    'description': row[2],
                    'base_price': row[3],
                    'category_id': row[4],
                    'seller_id': row[5],
                    'product_rating': row[6],
                    'is_active': row[7],
                    'image_url': row[8],
                    'variants': []
                }
            if row[9]:  # If variant exists
                products[product_id]['variants'].append({
                    'variant_id': row[9],
                    'color': row[10],
                    'size': row[11],
                    'stock_quantity': row[12],
                    'price': row[13],
                    'sku': row[14],
                    'is_active': row[15]
                })

        cursor.close()
        cnxn.close()
        return jsonify(list(products.values())), 200
    except Exception as e:
        logger.error(f"Error in get_products: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

# API endpoint to get a product by id
@app.route('/products/<int:product_id>', methods=['GET'])
def get_product(product_id):
    """Get a product by ID"""
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        query = """
        SELECT p.*, c.category_name
        FROM Products p
        LEFT JOIN Categories c ON p.category_id = c.category_id
        LEFT JOIN Images i ON p.product_id = i.product_id
        LEFT JOIN Product_Variants v ON p.product_id = v.product_id
        WHERE p.product_id = %s
        """
        cursor.execute(query, (product_id,))
        row = cursor.fetchone()
        if row:
            product_data = {
                'product_id': row[0],
                'product_name': row[1],
                'description': row[2],
                'base_price': float(row[3]),
                'category_id': row[4],
                'seller_id': row[5],
                'rating': float(row[6]) if row[6] else 0.0,
                'is_active': bool(row[7]),
                'category_name': row[8]
            }
            cursor.close()
            cnxn.close()
            return jsonify(product_data), 200
        cursor.close()
        cnxn.close()
        return jsonify({'message': 'Product not found'}), 404
    except Exception as e:
        if 'cursor' in locals():
            cursor.close()
        if 'cnxn' in locals():
            cnxn.close()
        return jsonify({'error': str(e)}), 500

# API endpoint to update a product
@app.route('/products/<int:product_id>', methods=['PUT'])
def update_product(product_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json

    try:
        # Update base product information
        update_query = """
        UPDATE Products 
        SET product_name = %s, 
            description = %s, 
            base_price = %s, 
            category_id = %s, 
            seller_id = %s,
            product_rating = %s,
            is_active = %s
        WHERE product_id = %s
        """
        cursor.execute(update_query, (
            data['product_name'],
            data['description'],
            data['base_price'],
            data['category_id'],
            data['seller_id'],
            data.get('product_rating', 0.0),
            data.get('is_active', True),
            product_id
        ))

        # Handle variants
        variants = data.get('variants', [])
        for variant in variants:
            if 'variant_id' in variant:
                # Update existing variant
                update_variant_query = """
                UPDATE Product_Variants 
                SET color = %s, 
                    size = %s, 
                    stock_quantity = %s, 
                    price = %s,
                    sku = %s,
                    is_active = %s
                WHERE variant_id = %s AND product_id = %s
                """
                cursor.execute(update_variant_query, (
                    variant['color'],
                    variant['size'],
                    variant['stock_quantity'],
                    variant['price'],
                    variant.get('sku'),
                    variant.get('is_active', True),
                    variant['variant_id'],
                    product_id
                ))
            else:
                # Insert new variant
                insert_variant_query = """
                INSERT INTO Product_Variants 
                (product_id, color, size, stock_quantity, price, sku, is_active) 
                VALUES (%s, %s, %s, %s, %s, %s, %s)
                """
                cursor.execute(insert_variant_query, (
                    product_id,
                    variant['color'],
                    variant['size'],
                    variant['stock_quantity'],
                    variant['price'],
                    variant.get('sku'),
                    variant.get('is_active', True)
                ))

        cnxn.commit()
        cursor.close()
        cnxn.close()

        return jsonify({'message': 'Product updated successfully'}), 200

    except Exception as e:
        cnxn.rollback()
        cursor.close()
        cnxn.close()
        return jsonify({'error': f'Failed to update product: {str(e)}'}), 500

# API endpoint to delete a product
@app.route('/products/<product_id>', methods=['DELETE'])
def delete_product(product_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "DELETE FROM Products WHERE product_id = %s"
    cursor.execute(query, (product_id,))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Product deleted successfully'}), 200

# API endpoint to create a new image
@app.route('/images', methods=['POST'])
def create_image():
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json
    product_id = data['product_id']
    image_url = data['image_url']

    query = "INSERT INTO Images (product_id, image_url) VALUES (%s, %s)"
    cursor.execute(query, (product_id, image_url))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Image created successfully'}), 201

@app.route('/images', methods=['GET'])
def get_images():
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        query = "SELECT * FROM Images"
        cursor.execute(query)
        rows = cursor.fetchall()

        images = []
        for row in rows:
            image = {
                'image_id': row[0],
                'product_id': row[1],
                'image_url': row[2]
            }
            images.append(image)

        cursor.close()
        cnxn.close()
        return jsonify(images), 200
    except Exception as e:
        logger.error(f"Error in get_images: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

# API endpoint to get an image by id
@app.route('/images/<image_id>', methods=['GET'])
def get_image(image_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "SELECT * FROM Images WHERE image_id = %s"
    cursor.execute(query, (image_id,))
    row = cursor.fetchone()

    if row:
        image = {
            'image_id': row[0],
            'product_id': row[1],
            'image_url': row[2]
        }
        cursor.close()
        cnxn.close()
        return jsonify(image), 200
    else:
        cursor.close()
        cnxn.close()
        return jsonify({'message': 'Image not found'}), 404

# API endpoint to update an image
@app.route('/images/<image_id>', methods=['PUT'])
def update_image(image_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json
    product_id = data['product_id']
    image_url = data['image_url']

    query = "UPDATE Images SET product_id = %s, image_url = %s WHERE image_id = %s"
    cursor.execute(query, (product_id, image_url, image_id))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Image updated successfully'}), 200

# API endpoint to delete an image
@app.route('/images/<image_id>', methods=['DELETE'])
def delete_image(image_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "DELETE FROM Images WHERE image_id = %s"
    cursor.execute(query, (image_id,))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Image deleted successfully'}), 200

# API endpoint to create a new cart
@app.route('/cart', methods=['POST'])
def create_cart():
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json
    
    try:
        # Validate required fields
        if not data or 'email' not in data:
            return jsonify({'error': 'Email is required'}), 400

        email = data['email']

        # Check if account exists
        cursor.execute("SELECT email FROM Accounts WHERE email = %s", (email,))
        if not cursor.fetchone():
            return jsonify({'error': 'Account not found'}), 404

        # Check if cart already exists
        cursor.execute("SELECT cart_id FROM Cart WHERE email = %s", (email,))
        existing_cart = cursor.fetchone()
        
        if existing_cart:
            return jsonify({
                'message': 'Cart already exists',
                'cart_id': existing_cart[0]
            }), 200

        # Create new cart
        cursor.execute("""
            INSERT INTO Cart (email)
            VALUES (%s)
            RETURNING cart_id
        """, (email,))
        cart_id = cursor.fetchone()[0]
        cnxn.commit()

        return jsonify({
            'message': 'Cart created successfully',
            'cart_id': cart_id
        }), 201

    except Exception as e:
        logging.error(f"Error in create_cart: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500
    finally:
        cursor.close()
        cnxn.close()

@app.route('/cart', methods=['GET'])
def get_carts():
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        query = "SELECT * FROM Cart"
        cursor.execute(query)
        rows = cursor.fetchall()

        carts = []
        for row in rows:
            cart = {
                'cart_id': row[0],
                'email': row[1]
            }
            carts.append(cart)

        cursor.close()
        cnxn.close()
        return jsonify(carts), 200
    except Exception as e:
        logger.error(f"Error in get_carts: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

# API endpoint to get a cart by id
@app.route('/cart/<cart_id>', methods=['GET'])
def get_cart(cart_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "SELECT * FROM Cart WHERE cart_id = %s"
    cursor.execute(query, (cart_id,))
    row = cursor.fetchone()

    if row:
        cart = {
            'cart_id': row[0],
            'email': row[1]
        }
        cursor.close()
        cnxn.close()
        return jsonify(cart), 200
    else:
        cursor.close()
        cnxn.close()
        return jsonify({'message': 'Cart not found'}), 404

# API endpoint to update a cart
@app.route('/cart/<cart_id>', methods=['PUT'])
def update_cart(cart_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json
    email = data['email']

    query = "UPDATE Cart SET email = %s WHERE cart_id = %s"
    cursor.execute(query, (email, cart_id))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Cart updated successfully'}), 200

# API endpoint to delete a cart
@app.route('/cart/<cart_id>', methods=['DELETE'])
def delete_cart(cart_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "DELETE FROM Cart WHERE cart_id = %s"
    cursor.execute(query, (cart_id,))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Cart deleted successfully'}), 200

# API endpoint to create a new order
@app.route('/orders', methods=['POST'])
def create_order():
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json

    try:
        # Validate required fields
        required_fields = ['email', 'address', 'phone_num', 'total_price']
        for field in required_fields:
            if field not in data:
                return jsonify({'error': f'Missing required field: {field}'}), 400

        email = data['email']
        address = data['address']
        phone_num = data['phone_num']
        total_price = data['total_price']

        # Start transaction
        cursor.execute("BEGIN")

        try:
            # Check if account exists
            cursor.execute("SELECT email FROM Accounts WHERE email = %s", (email,))
            if not cursor.fetchone():
                cursor.execute("ROLLBACK")
                return jsonify({'error': 'Account not found'}), 404

            # Create order
            cursor.execute("""
                INSERT INTO Orders (email, address, phone_num, total_price) 
                VALUES (%s, %s, %s, %s)
                RETURNING order_id
            """, (email, address, phone_num, total_price))
            order_id = cursor.fetchone()[0]

            cursor.execute("COMMIT")
            return jsonify({
                'message': 'Order created successfully',
                'order_id': order_id
            }), 201

        except Exception as e:
            cursor.execute("ROLLBACK")
            logger.error(f"Transaction error in create_order: {e}")
            return jsonify({'error': 'Failed to create order'}), 500

    except Exception as e:
        logger.error(f"Error in create_order: {e}")
        return jsonify({'error': 'Internal server error'}), 500
    finally:
        cursor.close()
        cnxn.close()

@app.route('/orders', methods=['GET'])
def get_orders():
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        query = "SELECT * FROM Orders"
        cursor.execute(query)
        rows = cursor.fetchall()

        orders = []
        for row in rows:
            order = {
                'order_id': row[0],
                'email': row[1],
                'address': row[2],
                'phone_num': row[3],
                'total_price': row[4]
            }
            orders.append(order)

        cursor.close()
        cnxn.close()
        return jsonify(orders), 200
    except Exception as e:
        logger.error(f"Error in get_orders: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

# API endpoint to get an order by id
@app.route('/orders/<order_id>', methods=['GET'])
def get_order(order_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "SELECT * FROM Orders WHERE order_id = %s"
    cursor.execute(query, (order_id,))
    row = cursor.fetchone()

    if row:
        order = {
            'order_id': row[0],
            'email': row[1],
            'address': row[2],
            'phone_num': row[3],
            'total_price': row[4]
        }
        cursor.close()
        cnxn.close()
        return jsonify(order), 200
    else:
        cursor.close()
        cnxn.close()
        return jsonify({'message': 'Order not found'}), 404

# API endpoint to update an order
@app.route('/orders/<order_id>', methods=['PUT'])
def update_order(order_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json
    email = data['email']
    address = data['address']
    phone_num = data['phone_num']
    total_price = data['total_price']

    query = "UPDATE Orders SET email = %s, address = %s, phone_num = %s, total_price = %s WHERE order_id = %s"
    cursor.execute(query, (email, address, phone_num, total_price, order_id))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Order updated successfully'}), 200

# API endpoint to delete an order
@app.route('/orders/<order_id>', methods=['DELETE'])
def delete_order(order_id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "DELETE FROM Orders WHERE order_id = %s"
    cursor.execute(query, (order_id,))
    cnxn.commit()
    cursor.close()
    cnxn.close()

    return jsonify({'message': 'Order deleted successfully'}), 200

# API endpoint to create a new cart product
@app.route('/cart-products', methods=['POST'])
def create_cart_product():
    cnxn = get_db_connection()
    cursor = cnxn.cursor()

    try:
        data = request.get_json()
        if not data or 'variant_id' not in data or 'cart_id' not in data or 'quantity' not in data:
            return jsonify({'error': 'Missing required fields'}), 400

        variant_id = data['variant_id']
        cart_id = data['cart_id']
        quantity = data['quantity']

        # Start transaction
        cursor.execute("BEGIN")

        # Check if cart exists
        cursor.execute("SELECT cart_id FROM Cart WHERE cart_id = %s", (cart_id,))
        if not cursor.fetchone():
            cursor.execute("ROLLBACK")
            return jsonify({'error': 'Cart not found'}), 404

        # Check if product variant exists and has enough stock
        cursor.execute("""
            SELECT price, stock_quantity 
            FROM Product_Variants 
            WHERE variant_id = %s
        """, (variant_id,))
        variant = cursor.fetchone()
        if not variant:
            cursor.execute("ROLLBACK")
            return jsonify({'error': 'Product variant not found'}), 404
        
        price, stock = variant
        if stock < quantity:
            cursor.execute("ROLLBACK")
            return jsonify({'error': 'Insufficient stock'}), 400

        # Check if product already exists in cart
        cursor.execute("""
            SELECT cart_id, quantity 
            FROM Cart_Products 
            WHERE cart_id = %s AND variant_id = %s
        """, (cart_id, variant_id))
        existing = cursor.fetchone()

        if existing:
            # Update quantity if product exists
            cart_product_id, current_quantity = existing
            new_quantity = current_quantity + quantity
            cursor.execute("""
                UPDATE Cart_Products 
                SET quantity = %s 
                WHERE cart_id = %s
            """, (new_quantity, cart_product_id))
        else:
            # Insert new product if it doesn't exist
            cursor.execute("""
                INSERT INTO Cart_Products (cart_id, variant_id, quantity, price_at_time)
                VALUES (%s, %s, %s, %s)
                RETURNING id
            """, (cart_id, variant_id, quantity, price))
            cart_product_id = cursor.fetchone()[0]

        cursor.execute("COMMIT")
        return jsonify({
            'message': 'Product added to cart successfully',
            'cart_product_id': cart_product_id
        }), 201

    except Exception as e:
        cursor.execute("ROLLBACK")
        logging.error(f"Error in create_cart_product: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500
    finally:
        cursor.close()
        cnxn.close()


@app.route('/cart-products', methods=['GET'])
def get_cart_products():
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        query = "SELECT * FROM Cart_Products"
        cursor.execute(query)
        rows = cursor.fetchall()

        cart_products = []
        for row in rows:
            cart_product = {
            'variant_id': row[0],  # variant_id
            'cart_id': row[1],     # cart_id
            'order_id': row[2],    # order_id
            'quantity': row[3],    # quantity
            'price_at_time': row[4],  # price_at_time
            'created_at': row[5]   # created_at
        }

            cart_products.append(cart_product)

        cursor.close()
        cnxn.close()
        return jsonify(cart_products), 200
    except Exception as e:
        logger.error(f"Error in get_cart_products: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

# API endpoint to get a cart product by id
@app.route('/cart-products/<id>', methods=['GET'])
def get_cart_product(id):
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    query = "SELECT * FROM Cart_Products WHERE id = %s"
    cursor.execute(query, (id,))
    row = cursor.fetchone()

    if row:
        cart_product = {
            'variant_id': row[0],  # variant_id
            'cart_id': row[1],     # cart_id
            'order_id': row[2],    # order_id
            'quantity': row[3],    # quantity
            'price_at_time': row[4],  # price_at_time
            'created_at': row[5]   # created_at
        }

        cursor.close()
        cnxn.close()
        return jsonify(cart_product), 200
    else:
        cursor.close()
        cnxn.close()
        return jsonify({'message': 'Cart product not found'}), 404

@app.route('/cart/account/<string:email>', methods=['GET'])
def get_cart_for_account(email):
    cnxn = None
    cursor = None
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()

        # Check if account exists
        cursor.execute("SELECT email FROM Accounts WHERE email = %s", (email,))
        if not cursor.fetchone():
            return jsonify({'error': 'Account not found'}), 404

        # Get cart and cart products for the account
        cursor.execute("""
           SELECT c.cart_id, c.email, cp.variant_id, cp.quantity, cp.price_at_time,
                   p.product_name, pv.price
            FROM Cart c
            LEFT JOIN Cart_Products cp ON c.cart_id = cp.cart_id
            LEFT JOIN Product_Variants pv ON cp.variant_id = pv.variant_id
            LEFT JOIN Products p ON pv.product_id = p.product_id
            WHERE c.email = %s
        """, (email,))
        
        results = cursor.fetchall()
        
        if not results:
            return jsonify({'cart_id': None, 'email': email, 'products': []}), 200

        # Group products by cart
        carts = {}
        for row in results:
            cart_id = row[0]
            if cart_id not in carts:
                carts[cart_id] = {
                    'cart_id': cart_id,
                    'email': row[1],
                    'products': []
                }
            
            if row[2] is not None:  # If there are products in the cart
                carts[cart_id]['products'].append({
                    'variant_id': row[2],
                    'quantity': row[3],
                    'price_at_time': row[4],
                    'product_name': row[5],
                    'current_price': row[6]
                })

        return jsonify(list(carts.values())), 200

    except Exception as e:
        logger.error(f"Error in get_cart_for_account: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

    finally:
        if cursor:
            cursor.close()
        if cnxn:
            cnxn.close()
@app.route('/cart-products', methods=['PUT'])
def update_product_quantity():
    cnxn = get_db_connection()
    cursor = cnxn.cursor()
    data = request.json
    cart_id = data.get('cart_id')
    product_id = data.get('product_id')
    quantity = data.get('quantity')

    # Validate required fields
    if not cart_id or not product_id or not quantity:
        cursor.close()
        cnxn.close()
        return jsonify({'error': 'Missing required fields'}), 400

    try:
        # التحقق من توفر الكمية المطلوبة في المخزون
        stock_query = """
        SELECT stock_quantity 
        FROM Product_Variants 
        WHERE product_id = %s
        """
        cursor.execute(stock_query, (product_id,))
        stock_row = cursor.fetchone()

        if not stock_row:
            cursor.close()
            cnxn.close()
            return jsonify({'error': 'Product variant not found'}), 404
        
        stock_quantity = stock_row[0]

        if quantity > stock_quantity:
            cursor.close()
            cnxn.close()
            return jsonify({'error': 'Requested quantity exceeds available stock'}), 400

        # تحديث الكمية في السلة
        update_query = """
        UPDATE Cart_Products
        SET quantity = %s
        WHERE cart_id = %s AND product_id = %s
        """
        cursor.execute(update_query, (quantity, cart_id, product_id))
        cnxn.commit()

        cursor.close()
        cnxn.close()
        return jsonify({'message': 'Product quantity updated successfully'}), 200

    except Exception as e:
        cnxn.rollback()
        cursor.close()
        cnxn.close()
        return jsonify({'error': f'Failed to update product quantity: {str(e)}'}), 500
@app.route('/variants/<int:variant_id>/check-availability', methods=['GET'])
def check_variant_availability(variant_id):
    """Check variant availability"""
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        query = """
        SELECT v.variant_id, v.color, v.size, v.stock_quantity, p.product_name
        FROM Product_Variants v
        JOIN Products p ON v.product_id = p.product_id
        WHERE v.variant_id = %s
        """
        cursor.execute(query, (variant_id,))
        row = cursor.fetchone()
        
        if not row:
            cursor.close()
            cnxn.close()
            return jsonify({'message': 'Variant not found'}), 404
        
        availability_data = {
            'variant_id': row[0],
            'color': row[1],
            'size': row[2],
            'stock_quantity': row[3],
            'product_name': row[4]
        }
        
        cursor.close()
        cnxn.close()
        return jsonify(availability_data), 200
    except Exception as e:
        if 'cursor' in locals():
            cursor.close()
        if 'cnxn' in locals():
            cnxn.close()
        return jsonify({'error': str(e)}), 500

@app.route('/routes', methods=['GET'])
def list_routes():
    routes = []
    for rule in app.url_map.iter_rules():
        routes.append(str(rule))
    return jsonify(routes), 200

class APIError(Exception):
    """Base exception class for API errors"""
    def __init__(self, message, status_code=400, payload=None):
        super().__init__()
        self.message = message
        self.status_code = status_code
        self.payload = payload

    def to_dict(self):
        rv = dict(self.payload or ())
        rv['message'] = self.message
        rv['status'] = 'error'
        return rv

@app.errorhandler(APIError)
def handle_api_error(error):
    response = jsonify(error.to_dict())
    response.status_code = error.status_code
    return response

@app.errorhandler(404)
def not_found_error(error):
    return jsonify({
        'status': 'error',
        'message': 'Resource not found'
    }), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({
        'status': 'error',
        'message': 'Internal server error'
    }), 500

def success_response(data=None, message=None, status_code=200):
    response = {
        'status': 'success',
        'data': data
    }
    if message:
        response['message'] = message
    return jsonify(response), status_code

@app.route('/products/random', methods=['GET'])
def get_random_products():
    """Get random products"""
    try:
        cnxn = get_db_connection()
        cursor = cnxn.cursor()
        query = """
        SELECT p.*, c.category_name
        FROM Products p
        LEFT JOIN Categories c ON p.category_id = c.category_id
        WHERE p.is_active = true
        ORDER BY RANDOM()
        LIMIT 10
        """
        cursor.execute(query)
        rows = cursor.fetchall()
        products = []
        for row in rows:
            product_data = {
                'product_id': row[0],
                'product_name': row[1],
                'description': row[2],
                'base_price': float(row[3]),
                'category_id': row[4],
                'seller_id': row[5],
                'rating': float(row[6]) if row[6] else 0.0,
                'is_active': bool(row[7]),
                'category_name': row[8]
            }
            products.append(product_data)
        cursor.close()
        cnxn.close()
        return jsonify(products), 200
    except Exception as e:
        if 'cursor' in locals():
            cursor.close()
        if 'cnxn' in locals():
            cnxn.close()
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True) 