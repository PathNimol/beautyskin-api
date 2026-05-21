-- Replace broken Rocket CDN / empty product images with Unsplash (one-time cleanup).
UPDATE products
SET image = 'https://images.unsplash.com/photo-1681487785847-c76e0dfd90e0?auto=format&fit=crop&w=1200&q=80',
    image_alt = COALESCE(NULLIF(TRIM(image_alt), ''), name || ' product photo'),
    updated_at = NOW()
WHERE deleted = FALSE
  AND (image IS NULL OR TRIM(image) = '' OR LOWER(image) LIKE '%rocket%');

UPDATE product_images pi
SET src = p.image, alt = p.image_alt
FROM products p
WHERE pi.product_id = p.id
  AND p.deleted = FALSE
  AND (pi.src IS NULL OR TRIM(pi.src) = '' OR LOWER(pi.src) LIKE '%rocket%');
