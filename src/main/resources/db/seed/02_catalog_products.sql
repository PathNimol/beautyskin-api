-- Catalog products + gallery rows. Idempotent by SKU (requires shops from 01_demo_shops.sql).

DROP FUNCTION IF EXISTS bs_upsert_product CASCADE;

CREATE OR REPLACE FUNCTION bs_upsert_product(
    p_id UUID,
    p_sku TEXT,
    p_name TEXT,
    p_brand TEXT,
    p_category TEXT,
    p_price NUMERIC,
    p_original_price NUMERIC,
    p_rating DOUBLE PRECISION,
    p_review_count INT,
    p_image TEXT,
    p_image_alt TEXT,
    p_stock INT,
    p_sold INT,
    p_status TEXT,
    p_shop_slug TEXT,
    p_tag TEXT
) RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    v_shop_id UUID;
    v_desc TEXT;
BEGIN
    SELECT id INTO v_shop_id FROM shops WHERE slug = p_shop_slug AND deleted = FALSE LIMIT 1;
    IF v_shop_id IS NULL THEN
        RETURN;
    END IF;

    v_desc := 'Premium ' || p_brand || ' formula curated for BS Online Shop — ' || lower(p_category) || ' routine.';

    IF EXISTS (SELECT 1 FROM products WHERE sku = p_sku AND deleted = FALSE) THEN
        UPDATE products
        SET image = p_image, image_alt = p_image_alt, original_price = p_original_price,
            price = p_price, stock = p_stock, sold = p_sold, status = p_status, updated_at = NOW()
        WHERE sku = p_sku AND deleted = FALSE;
        DELETE FROM product_images WHERE product_id IN (SELECT id FROM products WHERE sku = p_sku AND deleted = FALSE);
        INSERT INTO product_images (product_id, src, alt)
        SELECT id, p_image, p_image_alt FROM products WHERE sku = p_sku AND deleted = FALSE;
        RETURN;
    END IF;

    INSERT INTO products (
        id, created_at, updated_at, deleted,
        name, brand, category, price, original_price, stock, sold,
        rating, review_count, image, image_alt, description, how_to_use,
        expiry_date, sku, shop_id, status, weight, origin, visible, revoked
    ) VALUES (
        p_id, NOW(), NOW(), FALSE,
        p_name, p_brand, p_category, p_price, p_original_price, p_stock, p_sold,
        p_rating, p_review_count, p_image, p_image_alt, v_desc,
        'Apply to clean skin as directed on packaging. Patch test before first use.',
        CURRENT_DATE + INTERVAL '2 years', p_sku, v_shop_id, p_status, '50ml', 'South Korea', TRUE, FALSE
    );

    INSERT INTO product_images (product_id, src, alt) VALUES (p_id, p_image, p_image_alt);
    INSERT INTO product_ingredients (product_id, ingredient) VALUES
        (p_id, 'Water'), (p_id, 'Glycerin'), (p_id, 'Niacinamide'), (p_id, 'Hyaluronic Acid'), (p_id, 'Plant extracts');
    INSERT INTO product_skin_types (product_id, skin_type) VALUES (p_id, 'All'), (p_id, 'Combination');
    IF p_tag IS NOT NULL AND p_tag <> '' THEN
        INSERT INTO product_tags (product_id, tag) VALUES (p_id, p_tag);
    END IF;
END;
$$;

-- Showcase (8)
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000001','UI-SHOWCASE-001','Glow Essence Serum','COSRX','Serums',28.99,38.99,4.9,1247,'https://images.unsplash.com/photo-1618384874910-9f823a21babb?auto=format&fit=crop&w=1200&q=80','Clear glass serum bottle with white dropper cap on soft pink background',180,4200,'ACTIVE','glowskin','bestseller');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000002','UI-SHOWCASE-002','Hydra Barrier Cream','Laneige','Moisturizers',34.00,NULL,4.8,893,'https://images.unsplash.com/photo-1685526724067-d57ebf14903d?auto=format&fit=crop&w=1200&q=80','White cream jar with minimalist label on marble surface',120,3800,'ACTIVE','glowskin','new');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000003','UI-SHOWCASE-003','Snail Mucin Essence','COSRX','Serums',22.50,29.00,4.9,2103,'https://images.unsplash.com/photo-1681487785847-c76e0dfd90e0?auto=format&fit=crop&w=1200&q=80','Translucent essence bottle with minimalist Korean label',200,5100,'ACTIVE','kbeauty','bestseller');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000004','UI-SHOWCASE-004','Gentle Foam Cleanser','Innisfree','Cleansers',15.99,NULL,4.7,654,'https://images.unsplash.com/photo-1695561115616-b4b719f1a242?auto=format&fit=crop&w=1200&q=80','Green foam cleanser tube with botanical design',240,2900,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000005','UI-SHOWCASE-005','UV Shield SPF 50+','Skin1004','Sunscreen',19.99,24.99,4.8,421,'https://images.unsplash.com/photo-1624746478154-4b6aafbe77b5?auto=format&fit=crop&w=1200&q=80','White sunscreen tube with minimal packaging',95,2600,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000006','UI-SHOWCASE-006','Ceramide Repair Toner','Dr. Jart+','Serums',42.00,NULL,4.6,318,'https://images.unsplash.com/photo-1616526629549-353331fea648?auto=format&fit=crop&w=1200&q=80','Blue toner bottle with medical-inspired packaging',110,2100,'ACTIVE','glowskin','staff_pick');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000007','UI-SHOWCASE-007','Rice Water Brightener','I''m From','Moisturizers',31.00,40.00,4.7,567,'https://images.unsplash.com/photo-1595300398913-3772655443e1?auto=format&fit=crop&w=1200&q=80','White essence bottle with rice grain design',130,3200,'ACTIVE','kbeauty','trending');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000008','UI-SHOWCASE-008','Centella Calming Gel','Purito','Moisturizers',17.50,NULL,4.8,789,'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=1200&q=80','Green gel moisturizer tube with centella illustration',0,2400,'OUT_OF_STOCK','glowskin','');

-- Extended (12)
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000009','CAT-EXTRA-001','Peptide Eye Contour Serum','The Ordinary','Eye Care',24.50,29.90,4.6,512,'https://images.unsplash.com/photo-1714980716170-64cae2744604?auto=format&fit=crop&w=1200&q=80','Eye serum roller tube on neutral flat lay',140,890,'ACTIVE','glowskin','new');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000000a','CAT-EXTRA-002','Overnight Hydrating Mask','Glow Recipe','Masks & Treatments',38.00,NULL,4.8,721,'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=1200&q=80','Pink jelly sleeping mask jar with spatula',88,1750,'ACTIVE','kbeauty','bestseller');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000000b','CAT-EXTRA-003','Niacinamide 10% Serum','Paula''s Choice','Serums',29.00,NULL,4.7,933,'https://images.unsplash.com/photo-1617897903246-719242758050?auto=format&fit=crop&w=1200&q=80','Opaque serum bottle with pipette on white',165,2100,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000000c','CAT-EXTRA-004','Cloud Jelly Cleanser','Then I Met You','Cleansers',21.00,26.00,4.5,288,'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=1200&q=80','Soft teal cleanser tube minimal styling',190,1340,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000000d','CAT-EXTRA-005','Mineral Fluid SPF 45','Isntree','Sunscreen',18.50,NULL,4.7,615,'https://images.unsplash.com/photo-1681810814668-b498f00d4530?auto=format&fit=crop&w=1200&q=80','Sunscreen bottle with sea minerals branding',72,980,'LOW_STOCK','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000000e','CAT-EXTRA-006','Caffeine Eye Gel','The Inkey List','Eye Care',12.99,NULL,4.4,402,'https://images.unsplash.com/photo-1714980716170-64cae2744604?auto=format&fit=crop&w=1200&q=80','Small squeeze tube eye gel minimal',310,760,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000000f','CAT-EXTRA-007','Heartleaf Soothing Ampoule','Anua','Serums',27.50,34.00,4.8,1205,'https://images.unsplash.com/photo-1618384874910-9f823a21babb?auto=format&fit=crop&w=1200&q=80','Green glass ampoule bottle botanical skincare',155,3050,'ACTIVE','glowskin','trending');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000010','CAT-EXTRA-008','Honey Glow Sheet Mask Set','I''m From','Masks & Treatments',24.00,NULL,4.6,377,'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=1200&q=80','Sheet masks packaging flat lay pastel',220,1120,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000011','CAT-EXTRA-009','Green Tea Foam Cleanser','Innisfree','Cleansers',14.50,NULL,4.5,891,'https://images.unsplash.com/photo-1695561115616-b4b719f1a242?auto=format&fit=crop&w=1200&q=80','Green tea cleanser tube botanical',260,1880,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000012','CAT-EXTRA-010','Barrier Repair Moisturizer','Etude House','Moisturizers',26.99,32.99,4.6,445,'https://images.unsplash.com/photo-1617897903246-719242758050?auto=format&fit=crop&w=1200&q=80','Pink moisturizer tube Korean branding',134,990,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000013','CAT-EXTRA-011','Vitamin Tree Serum','By Wishtrend','Serums',33.50,NULL,4.7,556,'https://images.unsplash.com/photo-1681810814668-b498f00d4530?auto=format&fit=crop&w=1200&q=80','Orange toned serum bottle vitamin skincare',108,720,'ACTIVE','glowskin','new');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000014','CAT-EXTRA-012','Calming Panthenol Cream','Soon Jung','Moisturizers',20.00,NULL,4.8,688,'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=1200&q=80','Minimal white tube sensitive skin cream',176,1410,'ACTIVE','kbeauty','');

-- Additional 30 (013–042)
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000015','CAT-EXTRA-013','AHA BHA PHA Toner','COSRX','Serums',19.99,24.99,4.7,445,'https://images.unsplash.com/photo-1618384874910-9f823a21babb?auto=format&fit=crop&w=1200&q=80','Exfoliating toner bottle on white',200,2100,'ACTIVE','glowskin','bestseller');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000016','CAT-EXTRA-014','Water Sleeping Mask','Laneige','Masks & Treatments',32.00,38.00,4.8,892,'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=1200&q=80','Overnight sleeping mask jar pastel blue',95,3200,'ACTIVE','kbeauty','bestseller');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000017','CAT-EXTRA-015','Lip Sleeping Mask','Laneige','Moisturizers',24.00,NULL,4.9,1540,'https://images.unsplash.com/photo-1685526724067-d57ebf14903d?auto=format&fit=crop&w=1200&q=80','Berry lip mask pot on marble',180,4100,'ACTIVE','kbeauty','trending');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000018','CAT-EXTRA-016','Madagascar Centella Ampoule','Skin1004','Serums',23.50,28.00,4.8,678,'https://images.unsplash.com/photo-1681487785847-c76e0dfd90e0?auto=format&fit=crop&w=1200&q=80','Centella ampoule dropper bottle',150,2800,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000019','CAT-EXTRA-017','Low pH Morning Cleanser','COSRX','Cleansers',13.50,NULL,4.7,521,'https://images.unsplash.com/photo-1695561115616-b4b719f1a242?auto=format&fit=crop&w=1200&q=80','Gel cleanser tube minimal packaging',220,1900,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000001a','CAT-EXTRA-018','Retinol 0.5% Night Oil','Some By Mi','Serums',27.99,34.99,4.6,334,'https://images.unsplash.com/photo-1666025068567-31e8618c0be2?auto=format&fit=crop&w=1200&q=80','Retinol oil dropper amber glass',88,890,'ACTIVE','kbeauty','new');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000001b','CAT-EXTRA-019','Birch Juice Moisturizer','Round Lab','Moisturizers',29.50,NULL,4.7,412,'https://images.unsplash.com/photo-1685526724067-d57ebf14903d?auto=format&fit=crop&w=1200&q=80','Lightweight moisturizer tube birch label',140,1200,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000001c','CAT-EXTRA-020','Mugwort Essence','I''m From','Serums',30.00,36.00,4.8,556,'https://images.unsplash.com/photo-1595300398913-3772655443e1?auto=format&fit=crop&w=1200&q=80','Mugwort essence bottle warm tone',125,1650,'ACTIVE','kbeauty','trending');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000001d','CAT-EXTRA-021','Propolis Light Ampoule','COSRX','Serums',28.00,NULL,4.9,987,'https://images.unsplash.com/photo-1618384874910-9f823a21babb?auto=format&fit=crop&w=1200&q=80','Golden propolis ampoule glass bottle',160,2900,'ACTIVE','glowskin','bestseller');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000001e','CAT-EXTRA-022','Rice Toner','I''m From','Serums',25.00,30.00,4.7,623,'https://images.unsplash.com/photo-1595300398913-3772655443e1?auto=format&fit=crop&w=1200&q=80','Rice toner bottle cream background',175,1400,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000001f','CAT-EXTRA-023','Clean It Zero Balm','Banila Co','Cleansers',18.99,NULL,4.6,812,'https://images.unsplash.com/photo-1695561115616-b4b719f1a242?auto=format&fit=crop&w=1200&q=80','Cleansing balm jar pink lid',190,2200,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000020','CAT-EXTRA-024','Relief Sun Rice + Probiotics','Beauty of Joseon','Sunscreen',17.99,NULL,4.8,1102,'https://images.unsplash.com/photo-1624746478154-4b6aafbe77b5?auto=format&fit=crop&w=1200&q=80','Korean sunscreen tube minimal',210,3500,'ACTIVE','kbeauty','bestseller');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000021','CAT-EXTRA-025','Volcanic Pore Cleanser','Innisfree','Cleansers',14.99,18.99,4.5,389,'https://images.unsplash.com/photo-1695561115616-b4b719f1a242?auto=format&fit=crop&w=1200&q=80','Volcanic cleanser tube green',200,980,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000022','CAT-EXTRA-026','Hyaluronic Acid Toner Plus','Isntree','Serums',22.00,NULL,4.7,478,'https://images.unsplash.com/photo-1616526629549-353331fea648?auto=format&fit=crop&w=1200&q=80','Hyaluronic toner bottle clear',165,1100,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000023','CAT-EXTRA-027','Fundamental Water Gel','Klairs','Moisturizers',27.00,32.00,4.6,301,'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=1200&q=80','Light gel moisturizer blue tube',130,870,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000024','CAT-EXTRA-028','Birch Juice Refining Toner','Round Lab','Serums',24.50,NULL,4.7,356,'https://images.unsplash.com/photo-1616526629549-353331fea648?auto=format&fit=crop&w=1200&q=80','Refining toner bottle birch label',145,920,'ACTIVE','glowskin','new');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000025','CAT-EXTRA-029','Ginseng Essence Water','Beauty of Joseon','Serums',26.00,31.00,4.8,744,'https://images.unsplash.com/photo-1681487785847-c76e0dfd90e0?auto=format&fit=crop&w=1200&q=80','Ginseng essence bottle amber',118,1800,'ACTIVE','kbeauty','trending');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000026','CAT-EXTRA-030','Mugwort Clay Mask','Isntree','Masks & Treatments',18.00,NULL,4.6,267,'https://images.unsplash.com/photo-1636467769776-6c8db2bf3b3c?auto=format&fit=crop&w=1200&q=80','Green clay mask tube botanical',175,640,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000027','CAT-EXTRA-031','Aloe Relief Sun Cream','Purito','Sunscreen',16.50,NULL,4.7,533,'https://images.unsplash.com/photo-1681810814668-b498f00d4530?auto=format&fit=crop&w=1200&q=80','Aloe sunscreen tube white green',92,1500,'LOW_STOCK','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000028','CAT-EXTRA-032','Bifida Complex Ampoule','Manyo','Serums',32.99,39.99,4.8,421,'https://images.unsplash.com/photo-1617897903246-719242758050?auto=format&fit=crop&w=1200&q=80','Ferment ampoule glass dropper',105,760,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000029','CAT-EXTRA-033','Gold Hydrogel Eye Patch','Petitfee','Eye Care',22.00,NULL,4.5,198,'https://images.unsplash.com/photo-1714980716170-64cae2744604?auto=format&fit=crop&w=1200&q=80','Gold eye patch jar luxury skincare',240,540,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000002a','CAT-EXTRA-034','Dear Darling Lip Tint','Etude House','Makeup',12.99,15.99,4.6,890,'https://images.unsplash.com/photo-1702312685548-3832748d09d6?auto=format&fit=crop&w=1200&q=80','Cherry lip tint tube cute packaging',310,2400,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000002b','CAT-EXTRA-035','Zero Velvet Tint','Romand','Makeup',14.50,NULL,4.7,672,'https://images.unsplash.com/photo-1702312685548-3832748d09d6?auto=format&fit=crop&w=1200&q=80','Matte lip tint rose shade',280,1900,'ACTIVE','kbeauty','new');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000002c','CAT-EXTRA-036','M Perfect Cover BB Cream','Missha','Makeup',18.99,22.99,4.5,445,'https://images.unsplash.com/photo-1702312685548-3832748d09d6?auto=format&fit=crop&w=1200&q=80','BB cream tube shade swatch',195,1300,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000002d','CAT-EXTRA-037','Snail Bee High Content Lotion','Benton','Moisturizers',21.00,NULL,4.8,512,'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=1200&q=80','Snail bee lotion pump bottle',155,1100,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000002e','CAT-EXTRA-038','Heartleaf 80% Soothing Ampoule','Anua','Serums',27.50,33.00,4.9,1305,'https://images.unsplash.com/photo-1681487785847-c76e0dfd90e0?auto=format&fit=crop&w=1200&q=80','Heartleaf ampoule green glass',140,3400,'ACTIVE','kbeauty','bestseller');
SELECT bs_upsert_product('10000000-0000-4000-8000-00000000002f','CAT-EXTRA-039','Discoloration Repair Serum','Good Molecules','Serums',14.00,NULL,4.4,289,'https://images.unsplash.com/photo-1666025068567-31e8618c0be2?auto=format&fit=crop&w=1200&q=80','Minimal serum bottle clinical label',220,680,'ACTIVE','glowskin','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000030','CAT-EXTRA-040','Ceramide Ato Concentrate','Real Barrier','Moisturizers',28.50,34.50,4.7,367,'https://images.unsplash.com/photo-1685526724067-d57ebf14903d?auto=format&fit=crop&w=1200&q=80','Ceramide cream tube sensitive skin',120,890,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000031','CAT-EXTRA-041','Dark Spot Correcting Glow Serum','Axis-Y','Serums',23.00,28.00,4.6,478,'https://images.unsplash.com/photo-1618384874910-9f823a21babb?auto=format&fit=crop&w=1200&q=80','Brightening serum bottle soft pink',135,1020,'ACTIVE','kbeauty','');
SELECT bs_upsert_product('10000000-0000-4000-8000-000000000032','CAT-EXTRA-042','Birch Juice Mild Cleanser','Round Lab','Cleansers',16.00,NULL,4.7,356,'https://images.unsplash.com/photo-1695561115616-b4b719f1a242?auto=format&fit=crop&w=1200&q=80','Mild cleanser foam tube birch',185,950,'ACTIVE','glowskin','');

DROP FUNCTION bs_upsert_product;
