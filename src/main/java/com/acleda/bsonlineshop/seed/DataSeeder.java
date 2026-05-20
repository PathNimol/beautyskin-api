package com.acleda.bsonlineshop.seed;

import com.acleda.bsonlineshop.entity.ChatRoom;
import com.acleda.bsonlineshop.entity.Product;
import com.acleda.bsonlineshop.entity.ProductImage;
import com.acleda.bsonlineshop.entity.Promotion;
import com.acleda.bsonlineshop.entity.Shop;
import com.acleda.bsonlineshop.entity.ShippingAddress;
import com.acleda.bsonlineshop.entity.User;
import com.acleda.bsonlineshop.enums.AccountStatus;
import com.acleda.bsonlineshop.enums.ProductStatus;
import com.acleda.bsonlineshop.enums.PromotionStatus;
import com.acleda.bsonlineshop.enums.PromotionType;
import com.acleda.bsonlineshop.enums.ShopPlan;
import com.acleda.bsonlineshop.enums.ShopStatus;
import com.acleda.bsonlineshop.enums.UserRole;
import com.acleda.bsonlineshop.repository.ChatRoomRepository;
import com.acleda.bsonlineshop.repository.ProductRepository;
import com.acleda.bsonlineshop.repository.PromotionRepository;
import com.acleda.bsonlineshop.repository.ShopRepository;
import com.acleda.bsonlineshop.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }
        log.info("Seeding BS Online Shop demo data...");

        Shop shop1 = createShop("GlowSkin Store", "glowskin", "Sarah Chen", ShopPlan.GROWTH, "Korean Skincare");
        Shop shop2 = createShop("K-Beauty Hub", "kbeauty", "Ji-Yeon Park", ShopPlan.ENTERPRISE, "K-Beauty");

        User admin    = createUser("admin@beautyskin.com",  "admin123", "Alex",    "Morgan",    UserRole.ADMIN,    null,          null);
        User owner    = createUser("owner@beautyskin.com",  "owner123", "Sarah",   "Chen",      UserRole.OWNER,    shop1.getId(), null);
        User owner2   = createUser("owner2@beautyskin.com", "owner123", "Ji-Yeon", "Park",      UserRole.OWNER,    shop2.getId(), null);
        User staff    = createUser("staff@beautyskin.com",  "staff123", "Mia",     "Johnson",   UserRole.STAFF,    shop1.getId(), null);
        User customer = createUser("buyer@beautyskin.com",  "buyer123", "Emma",    "Rodriguez", UserRole.CUSTOMER, null,          buildShipping());
        shop1.setOwnerId(owner.getId());
        shop2.setOwnerId(owner2.getId());
        shopRepository.save(shop1);
        shopRepository.save(shop2);

        seedProducts(shop1);
        seedProducts(shop2);
        seedPromotions(shop1);
        seedChatRooms();

        log.info("Seed complete. Demo accounts: admin@beautyskin.com / admin123, owner@beautyskin.com / owner123, staff@beautyskin.com / staff123, buyer@beautyskin.com / buyer123");
    }

    private Shop createShop(String name, String slug, String ownerName, ShopPlan plan, String category) {
        Shop shop = new Shop();
        shop.setName(name);
        shop.setSlug(slug);
        shop.setOwnerName(ownerName);
        shop.setDescription("Premium beauty products");
        shop.setLogo("https://img.rocket.new/generatedImages/rocket_gen_img_16c200659-1778572111354.png");
        shop.setLogoAlt(name + " logo");
        shop.setPlan(plan);
        shop.setStatus(ShopStatus.ACTIVE);
        shop.setCategory(category);
        shop.setRevenue(BigDecimal.valueOf(94200));
        shop.setOrdersCount(1284);
        shop.setProductsCount(87);
        shop.setCustomersCount(3247);
        return shopRepository.save(shop);
    }

    private User createUser(String email, String password, String firstName, String lastName, UserRole role, UUID shopId, ShippingAddress shipping) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setShopId(shopId);
        user.setAvatar("https://img.rocket.new/generatedImages/rocket_gen_img_default.png");
        user.setAvatarAlt(firstName + " " + lastName);
        user.setStatus(AccountStatus.ACTIVE);
        user.setJoinDate(Instant.now());
        user.setEmailVerified(true);
        user.setShipping(shipping);
        return userRepository.save(user);
    }

    private ShippingAddress buildShipping() {
        ShippingAddress s = new ShippingAddress();
        s.setFirstName("Emma");
        s.setLastName("Rodriguez");
        s.setAddress("123 Riverside Blvd, Apt 4B");
        s.setCity("Phnom Penh");
        s.setState("Phnom Penh");
        s.setZip("12000");
        s.setCountry("Cambodia");
        return s;
    }

    private void seedProducts(Shop shop) {
        List<String[]> products = List.of(
                new String[]{"COSRX Snail Mucin Essence", "COSRX", "Skincare", "24.99", "150"},
                new String[]{"Laneige Water Sleeping Mask", "Laneige", "Skincare", "32.00", "89"},
                new String[]{"Innisfree Green Tea Serum", "Innisfree", "Skincare", "28.50", "120"},
                new String[]{"Missha BB Cream SPF42", "Missha", "Makeup", "18.99", "200"},
                new String[]{"Etude House Lip Tint", "Etude House", "Makeup", "12.99", "300"});
        int i = 0;
        for (String[] p : products) {
            Product product = new Product();
            product.setShop(shop);
            product.setName(p[0]);
            product.setBrand(p[1]);
            product.setCategory(p[2]);
            product.setPrice(new BigDecimal(p[3]));
            product.setOriginalPrice(new BigDecimal(p[3]).multiply(BigDecimal.valueOf(1.2)));
            product.setStock(Integer.parseInt(p[4]));
            product.setSold(50 + i * 10);
            product.setRating(4.5 + (i % 3) * 0.1);
            product.setReviewCount(20 + i * 5);
            product.setSku("SKU-" + shop.getSlug().toUpperCase() + "-" + (1000 + i));
            product.setImage("https://img.rocket.new/generatedImages/rocket_gen_img_product.png");
            product.setImageAlt(p[0]);
            product.setImages(List.of(new ProductImage(product.getImage(), p[0])));
            product.setDescription("Premium " + p[1] + " product for radiant skin.");
            product.setIngredients(List.of("Water", "Glycerin", "Niacinamide"));
            product.setHowToUse("Apply after cleansing.");
            product.setSkinTypes(List.of("All", "Combination"));
            product.setExpiryDate(LocalDate.now().plusYears(2));
            product.setStatus(ProductStatus.ACTIVE);
            product.setTags(List.of("bestseller", "k-beauty"));
            product.setWeight("50ml");
            product.setOrigin("South Korea");
            product.setVisible(true);
            productRepository.save(product);
            i++;
        }
    }

    private void seedPromotions(Shop shop) {
        createPromo(shop, "Beauty 10% Off", "BEAUTY10", PromotionType.PERCENTAGE, "10", 50);
        createPromo(shop, "Skin Care 20%", "SKIN20", PromotionType.PERCENTAGE, "20", 30);
        createPromo(shop, "Save $5", "SAVE5", PromotionType.FIXED, "5", 100);
        createPromo(shop, "Welcome 15%", "WELCOME15", PromotionType.PERCENTAGE, "15", 200);
    }

    private void createPromo(Shop shop, String name, String code, PromotionType type, String value, int maxUses) {
        Promotion p = new Promotion();
        p.setShop(shop);
        p.setName(name);
        p.setCode(code);
        p.setType(type);
        p.setValue(new BigDecimal(value));
        p.setMinOrder(BigDecimal.valueOf(25));
        p.setMaxUses(maxUses);
        p.setUsedCount(0);
        p.setStartDate(LocalDate.now().minusDays(1));
        p.setEndDate(LocalDate.now().plusMonths(3));
        p.setStatus(PromotionStatus.ACTIVE);
        p.setDescription(name);
        promotionRepository.save(p);
    }

    private void seedChatRooms() {
        createRoom("General", "general", "admin,owner,staff,customer");
        createRoom("Shop Owners", "shop-owners", "admin,owner");
        createRoom("Staff Lounge", "staff", "admin,owner,staff");
        createRoom("Customer Support", "support", "admin,owner,staff,customer");
    }

    private void createRoom(String name, String type, String roles) {
        ChatRoom room = new ChatRoom();
        room.setName(name);
        room.setRoomType(type);
        room.setAllowedRoles(roles);
        chatRoomRepository.save(room);
    }
}
