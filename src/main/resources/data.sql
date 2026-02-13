-- 5 Drones - All in LOADING state
-- Drone weight limits: LIGHTWEIGHT=250g, MIDDLEWEIGHT=500g, CRUISERWEIGHT=750g, HEAVYWEIGHT=1000g
INSERT INTO drones (id, serial_number, model, state, battery_capacity) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'DRONE-001', 'LIGHTWEIGHT', 'LOADING', 100.0),
('550e8400-e29b-41d4-a716-446655440002', 'DRONE-002', 'MIDDLEWEIGHT', 'LOADING', 90.0),
('550e8400-e29b-41d4-a716-446655440003', 'DRONE-003', 'CRUISERWEIGHT', 'LOADING', 85.0),
('550e8400-e29b-41d4-a716-446655440004', 'DRONE-004', 'HEAVYWEIGHT', 'LOADING', 80.0),
('550e8400-e29b-41d4-a716-446655440005', 'DRONE-005', 'LIGHTWEIGHT', 'LOADING', 75.0);

-- Medications - Distributed to ensure weight < limit
-- DRONE-001 (LIGHTWEIGHT, limit 250g): 2 medications = 50g + 60g = 110g ✓
INSERT INTO medications (id, name, weight, code, image, drone_id) VALUES
('660e8400-e29b-41d4-a716-446655440001', 'Aspirin-500mg', 50.0, 'ASP_001', 'https://example.com/aspirin.jpg', '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655440002', 'Ibuprofen-200mg', 60.0, 'IBU_001', 'https://example.com/ibuprofen.jpg', '550e8400-e29b-41d4-a716-446655440001'),

-- DRONE-002 (MIDDLEWEIGHT, limit 500g): 3 medications = 75g + 80g + 40g = 195g ✓
('660e8400-e29b-41d4-a716-446655440003', 'Amoxicillin-500mg', 75.0, 'AMX_001', 'https://example.com/amoxicillin.jpg', '550e8400-e29b-41d4-a716-446655440002'),
('660e8400-e29b-41d4-a716-446655440004', 'Metformin-850mg', 80.0, 'MET_001', 'https://example.com/metformin.jpg', '550e8400-e29b-41d4-a716-446655440002'),
('660e8400-e29b-41d4-a716-446655440005', 'Lisinopril-10mg', 40.0, 'LIS_001', 'https://example.com/lisinopril.jpg', '550e8400-e29b-41d4-a716-446655440002'),

-- DRONE-003 (CRUISERWEIGHT, limit 750g): 4 medications = 45g + 35g + 100g + 90g = 270g ✓
('660e8400-e29b-41d4-a716-446655440006', 'Atorvastatin-20mg', 45.0, 'ATO_001', 'https://example.com/atorvastatin.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('660e8400-e29b-41d4-a716-446655440007', 'Omeprazole-20mg', 35.0, 'OME_001', 'https://example.com/omeprazole.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('660e8400-e29b-41d4-a716-446655440008', 'Insulin-Vial-100IU', 100.0, 'INS_001', 'https://example.com/insulin.jpg', '550e8400-e29b-41d4-a716-446655440003'),
('660e8400-e29b-41d4-a716-446655440009', 'Vitamin-D3-2000IU', 90.0, 'VIT_001', 'https://example.com/vitamin_d.jpg', '550e8400-e29b-41d4-a716-446655440003'),

-- DRONE-004 (HEAVYWEIGHT, limit 1000g): 5 medications = 85g + 55g + 50g + 60g + 100g = 350g ✓
('660e8400-e29b-41d4-a716-446655440010', 'Calcium-Carbonate-500mg', 85.0, 'CAL_001', 'https://example.com/calcium.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('660e8400-e29b-41d4-a716-446655440011', 'Loratadine-10mg', 55.0, 'LOR_001', 'https://example.com/loratadine.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('660e8400-e29b-41d4-a716-446655440012', 'Cetirizine-10mg', 50.0, 'CET_001', 'https://example.com/cetirizine.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('660e8400-e29b-41d4-a716-446655440013', 'Fexofenadine-180mg', 60.0, 'FEX_001', 'https://example.com/fexofenadine.jpg', '550e8400-e29b-41d4-a716-446655440004'),
('660e8400-e29b-41d4-a716-446655440014', 'Azithromycin-500mg', 100.0, 'AZI_001', 'https://example.com/azithromycin.jpg', '550e8400-e29b-41d4-a716-446655440004'),

-- DRONE-005 (LIGHTWEIGHT, limit 250g): 1 medication = 95g ✓
('660e8400-e29b-41d4-a716-446655440015', 'Doxycycline-100mg', 95.0, 'DOX_001', 'https://example.com/doxycycline.jpg', '550e8400-e29b-41d4-a716-446655440005');
