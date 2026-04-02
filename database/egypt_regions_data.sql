-- Sample data for Egypt regions and cities
-- This script inserts comprehensive sample data for testing the application

-- Insert Egypt regions
INSERT INTO regions (name, region_code) VALUES 
('Cairo', 'CAI'),
('Alexandria', 'ALX'),
('Giza', 'GIZ'),
('Qalyubia', 'QLB'),
('Sharqia', 'SHR'),
('Dakahlia', 'DKH'),
('Beheira', 'BHR'),
('Kafr El Sheikh', 'KFS'),
('Gharbia', 'GHB'),
('Monufia', 'MNF'),
('Damietta', 'DMT'),
('Port Said', 'PSD'),
('Ismailia', 'ISM'),
('Suez', 'SUZ'),
('North Sinai', 'NSI'),
('South Sinai', 'SSI'),
('Faiyum', 'FYM'),
('Beni Suef', 'BNS'),
('Minya', 'MIN'),
('Asyut', 'ASY'),
('Sohag', 'SHG'),
('Qena', 'QNA'),
('Luxor', 'LXR'),
('Aswan', 'ASN'),
('Red Sea', 'RSD'),
('New Valley', 'NVL'),
('Matrouh', 'MAT');

-- Insert cities for each region

-- Cairo cities (expanded list)
INSERT INTO cities (name, region_code, is_other) VALUES 
('Nasr City', 'CAI', FALSE),
('Maadi', 'CAI', FALSE),
('New Cairo', 'CAI', FALSE),
('Heliopolis', 'CAI', FALSE),
('Zamalek', 'CAI', FALSE),
('Dokki', 'CAI', FALSE),
('Mohandessin', 'CAI', FALSE),
('Shubra', 'CAI', FALSE),
('Helwan', 'CAI', FALSE),
('15th of May City', 'CAI', FALSE),
('Other', 'CAI', TRUE);

-- Alexandria cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Al-Montazah', 'ALX', FALSE),
('Al-Manshiya', 'ALX', FALSE),
('Bolkly', 'ALX', FALSE),
('Sidi Gaber', 'ALX', FALSE),
('Smouha', 'ALX', FALSE),
('Miami', 'ALX', FALSE),
('San Stefano', 'ALX', FALSE),
('Other', 'ALX', TRUE);

-- Giza cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Giza City', 'GIZ', FALSE),
('6th of October City', 'GIZ', FALSE),
('Sheikh Zayed City', 'GIZ', FALSE),
('Hawamdiya', 'GIZ', FALSE),
('Imbaba', 'GIZ', FALSE),
('Other', 'GIZ', TRUE);

-- Qalyubia cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Banha', 'QLB', FALSE),
('Qalyub', 'QLB', FALSE),
('Shubra El Kheima', 'QLB', FALSE),
('Khanka', 'QLB', FALSE),
('Other', 'QLB', TRUE);

-- Sharqia cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Zagazig', 'SHR', FALSE),
('10th of Ramadan City', 'SHR', FALSE),
('Abu Hammad', 'SHR', FALSE),
('Minya El Qamh', 'SHR', FALSE),
('Other', 'SHR', TRUE);

-- Dakahlia cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Mansoura', 'DKH', FALSE),
('Talkha', 'DKH', FALSE),
('Mit Ghamr', 'DKH', FALSE),
('Dekernes', 'DKH', FALSE),
('Other', 'DKH', TRUE);

-- Beheira cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Damanhur', 'BHR', FALSE),
('Kafr El Dawwar', 'BHR', FALSE),
('Rosetta', 'BHR', FALSE),
('Edku', 'BHR', FALSE),
('Other', 'BHR', TRUE);

-- Kafr El Sheikh cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Kafr El Sheikh', 'KFS', FALSE),
('Sidi Salem', 'KFS', FALSE),
('El Reyad', 'KFS', FALSE),
('Baltim', 'KFS', FALSE),
('Other', 'KFS', TRUE);

-- Gharbia cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Tanta', 'GHB', FALSE),
('El Mahalla El Kubra', 'GHB', FALSE),
('Kafr El Zayat', 'GHB', FALSE),
('Samanoud', 'GHB', FALSE),
('Other', 'GHB', TRUE);

-- Monufia cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Shibin El Kom', 'MNF', FALSE),
('Menouf', 'MNF', FALSE),
('Ashmoun', 'MNF', FALSE),
('Berkat El Sabaa', 'MNF', FALSE),
('Other', 'MNF', TRUE);

-- Damietta cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Damietta', 'DMT', FALSE),
('Faraskur', 'DMT', FALSE),
('Ras El Bar', 'DMT', FALSE),
('New Damietta', 'DMT', FALSE),
('Other', 'DMT', TRUE);

-- Port Said cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Port Said', 'PSD', FALSE),
('Port Fouad', 'PSD', FALSE),
('Other', 'PSD', TRUE);

-- Ismailia cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Ismailia', 'ISM', FALSE),
('Fayed', 'ISM', FALSE),
('Qantara', 'ISM', FALSE),
('Abu Sultan', 'ISM', FALSE),
('Other', 'ISM', TRUE);

-- Suez cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Suez', 'SUZ', FALSE),
('Arbaeen', 'SUZ', FALSE),
('Ataqah', 'SUZ', FALSE),
('Other', 'SUZ', TRUE);

-- North Sinai cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('El Arish', 'NSI', FALSE),
('Rafah', 'NSI', FALSE),
('Sheikh Zuweid', 'NSI', FALSE),
('Bir al-Abed', 'NSI', FALSE),
('Other', 'NSI', TRUE);

-- South Sinai cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Sharm El Sheikh', 'SSI', FALSE),
('Dahab', 'SSI', FALSE),
('Nuweiba', 'SSI', FALSE),
('Taba', 'SSI', FALSE),
('Saint Catherine', 'SSI', FALSE),
('Other', 'SSI', TRUE);

-- Faiyum cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Faiyum', 'FYM', FALSE),
('Ibsheway', 'FYM', FALSE),
('Tamiya', 'FYM', FALSE),
('Other', 'FYM', TRUE);

-- Beni Suef cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Beni Suef', 'BNS', FALSE),
('El Fashn', 'BNS', FALSE),
('Nasser', 'BNS', FALSE),
('Other', 'BNS', TRUE);

-- Minya cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Minya', 'MIN', FALSE),
('Mallawi', 'MIN', FALSE),
('Samalut', 'MIN', FALSE),
('Matay', 'MIN', FALSE),
('Other', 'MIN', TRUE);

-- Asyut cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Asyut', 'ASY', FALSE),
('Manfalut', 'ASY', FALSE),
('Abu Tig', 'ASY', FALSE),
('El Qusiya', 'ASY', FALSE),
('Other', 'ASY', TRUE);

-- Sohag cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Sohag', 'SHG', FALSE),
('Girga', 'SHG', FALSE),
('Akhmim', 'SHG', FALSE),
('Tahta', 'SHG', FALSE),
('Other', 'SHG', TRUE);

-- Qena cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Qena', 'QNA', FALSE),
('Luxor', 'QNA', FALSE),
('Nag Hammadi', 'QNA', FALSE),
('Abu Tesht', 'QNA', FALSE),
('Other', 'QNA', TRUE);

-- Luxor cities (separate governorate)
INSERT INTO cities (name, region_code, is_other) VALUES 
('Luxor City', 'LXR', FALSE),
('Karnak', 'LXR', FALSE),
('Esna', 'LXR', FALSE),
('Armant', 'LXR', FALSE),
('Other', 'LXR', TRUE);

-- Aswan cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Aswan', 'ASN', FALSE),
('Edfu', 'ASN', FALSE),
('Kom Ombo', 'ASN', FALSE),
('Abu Simbel', 'ASN', FALSE),
('Other', 'ASN', TRUE);

-- Red Sea cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Hurghada', 'RSD', FALSE),
('Marsa Alam', 'RSD', FALSE),
('El Gouna', 'RSD', FALSE),
('Ras Gharib', 'RSD', FALSE),
('Safaga', 'RSD', FALSE),
('Other', 'RSD', TRUE);

-- New Valley cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Kharga', 'NVL', FALSE),
('Dakhla', 'NVL', FALSE),
('Farafra', 'NVL', FALSE),
('Baranis', 'NVL', FALSE),
('Other', 'NVL', TRUE);

-- Matrouh cities
INSERT INTO cities (name, region_code, is_other) VALUES 
('Marsa Matrouh', 'MAT', FALSE),
('El Alamein', 'MAT', FALSE),
('Sidi Barrani', 'MAT', FALSE),
('Siwa', 'MAT', FALSE),
('Other', 'MAT', TRUE);

-- Verify data insertion
SELECT 'Regions inserted: ' || COUNT(*) FROM regions;
SELECT 'Cities inserted: ' || COUNT(*) FROM cities;
