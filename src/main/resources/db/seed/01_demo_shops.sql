-- Demo shops (stable slugs for catalog product FK). Idempotent on slug.
INSERT INTO shops (
    id, created_at, updated_at, deleted,
    name, slug, owner_name, description, logo, logo_alt,
    status, plan, revenue, orders_count, products_count, customers_count, category
)
VALUES
    (
        'a0000000-0000-4000-8000-000000000001',
        NOW(), NOW(), FALSE,
        'GlowSkin Store', 'glowskin', 'Sarah Chen',
        'Premium beauty products',
        'https://images.unsplash.com/photo-1702312685548-3832748d09d6?auto=format&fit=crop&w=400&q=80',
        'GlowSkin Store logo',
        'ACTIVE', 'GROWTH', 94200, 1284, 50, 3247, 'Korean Skincare'
    ),
    (
        'a0000000-0000-4000-8000-000000000002',
        NOW(), NOW(), FALSE,
        'K-Beauty Hub', 'kbeauty', 'Ji-Yeon Park',
        'Premium beauty products',
        'https://images.unsplash.com/photo-1681487785847-c76e0dfd90e0?auto=format&fit=crop&w=400&q=80',
        'K-Beauty Hub logo',
        'ACTIVE', 'ENTERPRISE', 94200, 1284, 50, 3247, 'K-Beauty'
    )
ON CONFLICT (slug) DO NOTHING;
