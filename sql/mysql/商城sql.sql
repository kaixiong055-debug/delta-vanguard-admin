CREATE TABLE IF NOT EXISTS market_activity
(
    id                    bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键',
    title                 varchar(50) NOT NULL COMMENT '活动标题',
    activity_type         tinyint(4)  NOT NULL COMMENT '活动类型',
    status                tinyint(4)  NOT NULL COMMENT '活动状态',
    start_time            datetime    NOT NULL COMMENT '开始时间',
    end_time              datetime    NOT NULL COMMENT '结束时间',
    invalid_time          datetime COMMENT '失效时间',
    delete_time           datetime COMMENT '删除时间',
    time_limited_discount varchar(2000) COMMENT '限时折扣配置',
    full_privilege        varchar(2000) COMMENT '满减优惠配置',
    creator               varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time           datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater               varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time           datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted               tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    tenant_id             bigint(20)  NOT NULL COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '促销活动';

CREATE TABLE IF NOT EXISTS promotion_coupon_template
(
    id                   bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name                 varchar(255) NOT NULL COMMENT '优惠券名称',
    description          varchar(500) COMMENT '优惠券描述',
    status               int         NOT NULL COMMENT '状态',
    total_count          int         NOT NULL COMMENT '总发放数量',
    take_limit_count     int         NOT NULL COMMENT '每人限领数量',
    take_type            int         NOT NULL COMMENT '领取方式',
    use_price            int         NOT NULL COMMENT '使用门槛金额',
    product_scope        int         NOT NULL COMMENT '商品适用范围',
    product_scope_values varchar(500) COMMENT '适用商品ID集合',
    validity_type        int         NOT NULL COMMENT '有效期类型',
    valid_start_time     datetime COMMENT '有效开始时间',
    valid_end_time       datetime COMMENT '有效结束时间',
    fixed_start_term     int COMMENT '固定有效开始天数',
    fixed_end_term       int COMMENT '固定有效结束天数',
    discount_type        int         NOT NULL COMMENT '优惠类型',
    discount_percent     int COMMENT '折扣百分比',
    discount_price       int COMMENT '优惠金额',
    discount_limit_price int COMMENT '折扣上限',
    take_count           int         NOT NULL DEFAULT 0 COMMENT '已领取数量',
    use_count            int         NOT NULL DEFAULT 0 COMMENT '已使用数量',
    creator              varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time          datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater              varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time          datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted              tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '优惠劵模板';

CREATE TABLE IF NOT EXISTS promotion_coupon
(
    id                   bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    template_id          bigint      NOT NULL COMMENT '模板ID',
    name                 varchar(255) NOT NULL COMMENT '优惠券名称',
    status               int         NOT NULL COMMENT '状态',
    user_id              bigint      NOT NULL COMMENT '用户ID',
    take_type            int         NOT NULL COMMENT '领取方式',
    use_price            int         NOT NULL COMMENT '使用门槛金额',
    valid_start_time     datetime    NOT NULL COMMENT '有效开始时间',
    valid_end_time       datetime    NOT NULL COMMENT '有效结束时间',
    product_scope        int         NOT NULL COMMENT '商品适用范围',
    product_scope_values varchar(500) COMMENT '适用商品ID集合',
    discount_type        int         NOT NULL COMMENT '优惠类型',
    discount_percent     int COMMENT '折扣百分比',
    discount_price       int COMMENT '优惠金额',
    discount_limit_price int COMMENT '折扣上限',
    use_order_id         bigint COMMENT '使用订单ID',
    use_time             datetime COMMENT '使用时间',
    creator              varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time          datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater              varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time          datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted              tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '优惠劵';

CREATE TABLE IF NOT EXISTS promotion_reward_activity
(
    id              bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name            varchar(255) NOT NULL COMMENT '活动名称',
    status          int         NOT NULL COMMENT '活动状态',
    start_time      datetime    NOT NULL COMMENT '开始时间',
    end_time        datetime    NOT NULL COMMENT '结束时间',
    remark          varchar(500) COMMENT '活动备注',
    condition_type  int         NOT NULL COMMENT '优惠条件类型',
    product_scope   int         NOT NULL COMMENT '商品适用范围',
    product_spu_ids varchar(500) COMMENT '适用商品SPU ID',
    rules           varchar(2000) COMMENT '活动规则',
    creator         varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '满减送活动';

CREATE TABLE IF NOT EXISTS promotion_discount_activity
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        varchar(255) NOT NULL COMMENT '活动名称',
    status      int         NOT NULL COMMENT '活动状态',
    start_time  datetime    NOT NULL COMMENT '开始时间',
    end_time    datetime    NOT NULL COMMENT '结束时间',
    remark      varchar(500) COMMENT '活动备注',
    creator     varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '限时折扣活动';

CREATE TABLE IF NOT EXISTS promotion_discount_product
(
    id                  bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    activity_id         bigint      NOT NULL COMMENT '活动ID',
    spu_id              bigint      NOT NULL COMMENT '商品SPU ID',
    sku_id              bigint      NOT NULL COMMENT '商品SKU ID',
    discount_type       int         NOT NULL COMMENT '折扣类型',
    discount_percent    int COMMENT '折扣百分比',
    discount_price      int COMMENT '折扣金额',
    activity_name       varchar(255) NOT NULL COMMENT '活动名称',
    activity_status     int         NOT NULL COMMENT '活动状态',
    activity_start_time datetime    NOT NULL COMMENT '活动开始时间',
    activity_end_time   datetime    NOT NULL COMMENT '活动结束时间',
    creator             varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater             varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '限时折扣商品';

CREATE TABLE IF NOT EXISTS promotion_seckill_activity
(
    id                 bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    spu_id             bigint      NOT NULL COMMENT '商品SPU ID',
    name               varchar(255) NOT NULL COMMENT '活动名称',
    status             int         NOT NULL COMMENT '活动状态',
    remark             varchar(500) COMMENT '活动备注',
    start_time         varchar(100) NOT NULL COMMENT '开始时间',
    end_time           varchar(100) NOT NULL COMMENT '结束时间',
    sort               int         NOT NULL COMMENT '排序值',
    config_ids         varchar(500) NOT NULL COMMENT '秒杀时段配置ID',
    order_count        int         NOT NULL COMMENT '订单数量',
    user_count         int         NOT NULL COMMENT '参与用户数',
    total_price        int         NOT NULL COMMENT '总金额',
    total_limit_count  int COMMENT '总限购数量',
    single_limit_count int COMMENT '单人限购数量',
    stock              int COMMENT '库存',
    total_stock        int COMMENT '总库存',
    creator            varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater            varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    tenant_id          bigint      NOT NULL COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '秒杀活动';

CREATE TABLE IF NOT EXISTS promotion_seckill_config
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        varchar(255) NOT NULL COMMENT '时段名称',
    start_time  varchar(100) NOT NULL COMMENT '开始时间',
    end_time    varchar(100) NOT NULL COMMENT '结束时间',
    pic_url     varchar(500) NOT NULL COMMENT '时段图片',
    status      int         NOT NULL COMMENT '状态',
    creator     varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    tenant_id   bigint      NOT NULL COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '秒杀时段配置';

CREATE TABLE IF NOT EXISTS promotion_combination_activity
(
    id                 bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name               varchar(255) NOT NULL COMMENT '活动名称',
    spu_id             bigint COMMENT '商品SPU ID',
    total_limit_count  int         NOT NULL COMMENT '总限购数量',
    single_limit_count int         NOT NULL COMMENT '单人限购数量',
    start_time         varchar(100) NOT NULL COMMENT '开始时间',
    end_time           varchar(100) NOT NULL COMMENT '结束时间',
    user_size          int         NOT NULL COMMENT '拼团人数',
    total_num          int         NOT NULL COMMENT '总成团数',
    success_num        int         NOT NULL COMMENT '成功成团数',
    order_user_count   int         NOT NULL COMMENT '下单用户数',
    virtual_group      int         NOT NULL COMMENT '是否虚拟成团',
    status             int         NOT NULL COMMENT '活动状态',
    limit_duration     int         NOT NULL COMMENT '拼团时限',
    creator            varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater            varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    tenant_id          bigint      NOT NULL COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '拼团活动';

CREATE TABLE IF NOT EXISTS promotion_combination_record
(
    id                bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    activity_id       bigint      NOT NULL COMMENT '活动ID',
    combination_price int         NOT NULL COMMENT '拼团价格',
    spu_id            bigint      NOT NULL COMMENT '商品SPU ID',
    spu_name          varchar(255) NOT NULL COMMENT '商品名称',
    pic_url           varchar(500) COMMENT '商品图片',
    sku_id            bigint      NOT NULL COMMENT '商品SKU ID',
    count             int         NOT NULL COMMENT '购买数量',
    user_id           bigint      NOT NULL COMMENT '用户ID',
    nickname          varchar(100) COMMENT '用户昵称',
    avatar            varchar(500) COMMENT '用户头像',
    head_id           bigint      NOT NULL COMMENT '团长ID',
    status            int         NOT NULL COMMENT '拼团状态',
    order_id          bigint      NOT NULL COMMENT '订单ID',
    user_size         int         NOT NULL COMMENT '拼团人数',
    user_count        int         NOT NULL COMMENT '已参与人数',
    virtual_group     tinyint(1)  NOT NULL COMMENT '是否虚拟成团',
    expire_time       datetime COMMENT '过期时间',
    start_time        datetime COMMENT '开始时间',
    end_time          datetime COMMENT '结束时间',
    creator           varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater           varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '拼团记录';

CREATE TABLE IF NOT EXISTS promotion_article_category
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        varchar(255) NOT NULL COMMENT '分类名称',
    pic_url     varchar(500) COMMENT '分类图片',
    status      int         NOT NULL COMMENT '状态',
    sort        int         NOT NULL COMMENT '排序值',
    creator     varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    tenant_id   bigint      NOT NULL COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章分类表';

CREATE TABLE IF NOT EXISTS promotion_article
(
    id               bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    category_id      bigint      NOT NULL COMMENT '分类ID',
    title            varchar(255) NOT NULL COMMENT '文章标题',
    author           varchar(100) COMMENT '作者',
    pic_url          varchar(500) NOT NULL COMMENT '封面图片',
    introduction     varchar(500) COMMENT '简介',
    browse_count     varchar(100) COMMENT '浏览次数',
    sort             int         NOT NULL COMMENT '排序值',
    status           int         NOT NULL COMMENT '状态',
    spu_id           bigint      NOT NULL COMMENT '关联商品ID',
    recommend_hot    tinyint(1)  NOT NULL COMMENT '是否热门推荐',
    recommend_banner tinyint(1)  NOT NULL COMMENT '是否Banner推荐',
    content          text        NOT NULL COMMENT '文章内容',
    creator          varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater          varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    tenant_id        bigint      NOT NULL COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文章管理表';

CREATE TABLE IF NOT EXISTS promotion_diy_template
(
    id               bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name             varchar(255) NOT NULL COMMENT '模板名称',
    used             tinyint(1)  NOT NULL COMMENT '是否使用',
    used_time        varchar(100) COMMENT '使用时间',
    remark           varchar(500) COMMENT '备注',
    preview_pic_urls varchar(500) COMMENT '预览图片',
    property         text        NOT NULL COMMENT '模板配置',
    creator          varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater          varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    tenant_id        bigint      NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '装修模板';

CREATE TABLE IF NOT EXISTS promotion_diy_page
(
    id               bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    template_id      bigint      NOT NULL COMMENT '模板ID',
    name             varchar(255) NOT NULL COMMENT '页面名称',
    remark           varchar(500) COMMENT '备注',
    preview_pic_urls varchar(500) COMMENT '预览图片',
    property         text COMMENT '页面配置',
    creator          varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater          varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    tenant_id        bigint      NOT NULL COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '装修页面';

-- ==============================
-- 商品中心 yudao-module-product (9表)
-- ==============================

CREATE TABLE IF NOT EXISTS product_brand
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '品牌编号',
    name        varchar(128) NOT NULL COMMENT '品牌名称',
    pic_url     varchar(512) COMMENT '品牌图片',
    sort        int         NOT NULL DEFAULT 0 COMMENT '品牌排序',
    description varchar(256) COMMENT '品牌描述',
    status      tinyint     NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    creator     varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品品牌';

CREATE TABLE IF NOT EXISTS product_category
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '分类编号',
    parent_id   bigint      NOT NULL DEFAULT 0 COMMENT '父分类编号',
    name        varchar(128) NOT NULL COMMENT '分类名称',
    pic_url     varchar(512) COMMENT '移动端分类图',
    sort        int         NOT NULL DEFAULT 0 COMMENT '分类排序',
    status      tinyint     NOT NULL DEFAULT 0 COMMENT '开启状态（0开启 1关闭）',
    creator     varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类';

CREATE TABLE IF NOT EXISTS product_favorite
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    user_id     bigint      NOT NULL COMMENT '用户编号',
    spu_id      bigint      NOT NULL COMMENT '商品 SPU 编号',
    creator     varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品收藏';

CREATE TABLE IF NOT EXISTS product_comment
(
    id                 bigint      NOT NULL AUTO_INCREMENT COMMENT '评论编号',
    user_id            bigint      NOT NULL COMMENT '评价人用户编号',
    user_nickname      varchar(64) COMMENT '评价人名称',
    user_avatar        varchar(512) COMMENT '评价人头像',
    anonymous          bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否匿名',
    order_id           bigint      NOT NULL COMMENT '交易订单编号',
    order_item_id      bigint      NOT NULL COMMENT '交易订单项编号',
    spu_id             bigint      NOT NULL COMMENT '商品 SPU 编号',
    spu_name           varchar(255) NOT NULL COMMENT '商品 SPU 名称',
    sku_id             bigint      NOT NULL COMMENT '商品 SKU 编号',
    sku_pic_url        varchar(512) COMMENT '商品 SKU 图片地址',
    sku_properties     varchar(2000) COMMENT '属性数组（JSON）',
    visible            bit(1)      NOT NULL DEFAULT b'1' COMMENT '是否可见',
    scores             int         NOT NULL COMMENT '评分星级 1-5',
    description_scores int         NOT NULL COMMENT '描述星级 1-5',
    benefit_scores     int         NOT NULL COMMENT '服务星级 1-5',
    content            text COMMENT '评论内容',
    pic_urls           varchar(2000) COMMENT '评论图片地址数组（JSON）',
    reply_status       bit(1)      NOT NULL DEFAULT b'0' COMMENT '商家是否回复',
    reply_user_id      bigint COMMENT '回复管理员编号',
    reply_content      varchar(1000) COMMENT '商家回复内容',
    reply_time         datetime COMMENT '商家回复时间',
    creator            varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater            varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品评论';

CREATE TABLE IF NOT EXISTS product_browse_history
(
    id           bigint      NOT NULL AUTO_INCREMENT COMMENT '记录编号',
    spu_id       bigint      NOT NULL COMMENT '商品 SPU 编号',
    user_id      bigint      NOT NULL COMMENT '用户编号',
    user_deleted bit(1)      NOT NULL DEFAULT b'0' COMMENT '用户是否删除',
    creator      varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater      varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品浏览记录';

CREATE TABLE IF NOT EXISTS product_property
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name        varchar(128) NOT NULL COMMENT '名称',
    remark      varchar(256) COMMENT '备注',
    creator     varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品属性项';

CREATE TABLE IF NOT EXISTS product_property_value
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    property_id bigint      NOT NULL COMMENT '属性项编号',
    name        varchar(128) NOT NULL COMMENT '名称',
    remark      varchar(256) COMMENT '备注',
    creator     varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品属性值';

CREATE TABLE IF NOT EXISTS product_spu
(
    id                    bigint      NOT NULL AUTO_INCREMENT COMMENT '商品 SPU 编号',
    name                  varchar(255) NOT NULL COMMENT '商品名称',
    keyword               varchar(256) COMMENT '关键字',
    introduction          varchar(500) COMMENT '商品简介',
    description           text COMMENT '商品详情',
    category_id           bigint      NOT NULL COMMENT '商品分类编号',
    brand_id              bigint COMMENT '商品品牌编号',
    pic_url               varchar(512) COMMENT '商品封面图',
    slider_pic_urls       varchar(2000) COMMENT '商品轮播图（JSON）',
    sort                  int         NOT NULL DEFAULT 0 COMMENT '排序',
    status                tinyint     NOT NULL COMMENT '商品状态',
    spec_type             bit(1)      NOT NULL DEFAULT b'0' COMMENT '规格类型（0单规格 1多规格）',
    price                 int         NOT NULL DEFAULT 0 COMMENT '商品价格（分）',
    market_price          int COMMENT '市场价（分）',
    cost_price            int COMMENT '成本价（分）',
    stock                 int         NOT NULL DEFAULT 0 COMMENT '库存',
    delivery_types        varchar(256) COMMENT '配送方式数组（JSON）',
    delivery_template_id  bigint COMMENT '物流配置模板编号',
    give_integral         int         NOT NULL DEFAULT 0 COMMENT '赠送积分',
    sub_commission_type   bit(1)      NOT NULL DEFAULT b'0' COMMENT '分销类型',
    sales_count           int         NOT NULL DEFAULT 0 COMMENT '商品销量',
    virtual_sales_count   int         NOT NULL DEFAULT 0 COMMENT '虚拟销量',
    browse_count          int         NOT NULL DEFAULT 0 COMMENT '浏览量',
    creator               varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time           datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater               varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time           datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted               tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品 SPU';

CREATE TABLE IF NOT EXISTS product_sku
(
    id                      bigint      NOT NULL AUTO_INCREMENT COMMENT '商品 SKU 编号',
    spu_id                  bigint      NOT NULL COMMENT 'SPU 编号',
    properties              varchar(2000) COMMENT '属性数组（JSON）',
    price                   int         NOT NULL DEFAULT 0 COMMENT '商品价格（分）',
    market_price            int COMMENT '市场价（分）',
    cost_price              int COMMENT '成本价（分）',
    bar_code                varchar(64) COMMENT '商品条码',
    pic_url                 varchar(512) COMMENT '图片地址',
    stock                   int         NOT NULL DEFAULT 0 COMMENT '库存',
    weight                  double COMMENT '商品重量（kg）',
    volume                  double COMMENT '商品体积（m³）',
    first_brokerage_price   int COMMENT '一级分销佣金（分）',
    second_brokerage_price  int COMMENT '二级分销佣金（分）',
    sales_count             int         NOT NULL DEFAULT 0 COMMENT '商品销量',
    creator                 varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time             datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                 varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time             datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                 tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品 SKU';

-- ==============================
-- 营销中心 补充表（10张）
-- ==============================

CREATE TABLE IF NOT EXISTS promotion_banner
(
    id           bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    title        varchar(128) NOT NULL COMMENT '标题',
    url          varchar(512) COMMENT '跳转链接',
    pic_url      varchar(512) NOT NULL COMMENT '图片链接',
    sort         int         NOT NULL DEFAULT 0 COMMENT '排序',
    status       tinyint     NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    position     tinyint     NOT NULL COMMENT '定位',
    memo         varchar(256) COMMENT '备注',
    browse_count int         NOT NULL DEFAULT 0 COMMENT '点击次数',
    creator      varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater      varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'Banner管理';

CREATE TABLE IF NOT EXISTS promotion_bargain_activity
(
    id                 bigint      NOT NULL AUTO_INCREMENT COMMENT '砍价活动编号',
    name               varchar(255) NOT NULL COMMENT '砍价活动名称',
    start_time         datetime    NOT NULL COMMENT '活动开始时间',
    end_time           datetime    NOT NULL COMMENT '活动结束时间',
    status             tinyint     NOT NULL COMMENT '活动状态',
    spu_id             bigint      NOT NULL COMMENT '商品 SPU 编号',
    sku_id             bigint      NOT NULL COMMENT '商品 SKU 编号',
    bargain_first_price int        NOT NULL DEFAULT 0 COMMENT '砍价起始价格（分）',
    bargain_min_price  int         NOT NULL DEFAULT 0 COMMENT '砍价底价（分）',
    stock              int         NOT NULL DEFAULT 0 COMMENT '砍价库存',
    total_stock        int         NOT NULL DEFAULT 0 COMMENT '砍价总库存',
    help_max_count     int         NOT NULL DEFAULT 0 COMMENT '砍价人数',
    bargain_count      int         NOT NULL DEFAULT 0 COMMENT '帮砍次数',
    total_limit_count  int COMMENT '总限购数量',
    random_min_price   int         NOT NULL DEFAULT 0 COMMENT '每次砍价最小金额（分）',
    random_max_price   int         NOT NULL DEFAULT 0 COMMENT '每次砍价最大金额（分）',
    creator            varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater            varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted            tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '砍价活动';

CREATE TABLE IF NOT EXISTS promotion_bargain_help
(
    id           bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    activity_id  bigint      NOT NULL COMMENT '砍价活动编号',
    record_id    bigint      NOT NULL COMMENT '砍价记录编号',
    user_id      bigint      NOT NULL COMMENT '用户编号',
    reduce_price int         NOT NULL DEFAULT 0 COMMENT '减少价格（分）',
    creator      varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater      varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '砍价助力';

CREATE TABLE IF NOT EXISTS promotion_bargain_record
(
    id                  bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    user_id             bigint      NOT NULL COMMENT '用户编号',
    activity_id         bigint      NOT NULL COMMENT '砍价活动编号',
    spu_id              bigint      NOT NULL COMMENT '商品 SPU 编号',
    sku_id              bigint      NOT NULL COMMENT '商品 SKU 编号',
    bargain_first_price int         NOT NULL DEFAULT 0 COMMENT '砍价起始价格（分）',
    bargain_price       int         NOT NULL DEFAULT 0 COMMENT '当前砍价（分）',
    status              tinyint     NOT NULL COMMENT '砍价状态',
    end_time            datetime COMMENT '结束时间',
    order_id            bigint COMMENT '订单编号',
    creator             varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater             varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '砍价记录';

CREATE TABLE IF NOT EXISTS promotion_combination_product
(
    id                  bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    activity_id         bigint      NOT NULL COMMENT '拼团活动编号',
    spu_id              bigint      NOT NULL COMMENT '商品 SPU 编号',
    sku_id              bigint      NOT NULL COMMENT '商品 SKU 编号',
    combination_price   int         NOT NULL DEFAULT 0 COMMENT '拼团价格（分）',
    activity_status     tinyint     NOT NULL COMMENT '拼团商品状态',
    activity_start_time datetime    NOT NULL COMMENT '活动开始时间点',
    activity_end_time   datetime    NOT NULL COMMENT '活动结束时间点',
    creator             varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater             varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '拼团商品';

CREATE TABLE IF NOT EXISTS promotion_kefu_conversation
(
    id                         bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    user_id                    bigint      NOT NULL COMMENT '会话所属用户',
    last_message_time          datetime COMMENT '最后聊天时间',
    last_message_content       varchar(1000) COMMENT '最后聊天内容',
    last_message_content_type  tinyint     COMMENT '最后消息类型',
    admin_pinned               bit(1)      NOT NULL DEFAULT b'0' COMMENT '管理端置顶',
    user_deleted               bit(1)      NOT NULL DEFAULT b'0' COMMENT '用户是否可见',
    admin_deleted              bit(1)      NOT NULL DEFAULT b'0' COMMENT '管理员是否可见',
    admin_unread_message_count int         NOT NULL DEFAULT 0 COMMENT '管理员未读消息数',
    creator                    varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time                datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                    varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time                datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                    tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '客服会话';

CREATE TABLE IF NOT EXISTS promotion_kefu_message
(
    id              bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    conversation_id bigint      NOT NULL COMMENT '会话编号',
    sender_id       bigint      NOT NULL COMMENT '发送人编号',
    sender_type     tinyint     NOT NULL COMMENT '发送人类型',
    receiver_id     bigint      NOT NULL COMMENT '接收人编号',
    receiver_type   tinyint     NOT NULL COMMENT '接收人类型',
    content_type    tinyint     NOT NULL COMMENT '消息类型',
    content         text        NOT NULL COMMENT '消息',
    read_status     bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否已读',
    creator         varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '客服消息';

CREATE TABLE IF NOT EXISTS promotion_seckill_product
(
    id                  bigint      NOT NULL AUTO_INCREMENT COMMENT '秒杀参与商品编号',
    activity_id         bigint      NOT NULL COMMENT '秒杀活动 id',
    config_ids          varchar(500) COMMENT '秒杀时段 id（JSON）',
    spu_id              bigint      NOT NULL COMMENT '商品 SPU 编号',
    sku_id              bigint      NOT NULL COMMENT '商品 SKU 编号',
    seckill_price       int         NOT NULL DEFAULT 0 COMMENT '秒杀金额（分）',
    stock               int         NOT NULL DEFAULT 0 COMMENT '秒杀库存',
    activity_status     tinyint     NOT NULL COMMENT '秒杀商品状态',
    activity_start_time datetime    NOT NULL COMMENT '活动开始时间点',
    activity_end_time   datetime    NOT NULL COMMENT '活动结束时间点',
    creator             varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater             varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '秒杀参与商品';

CREATE TABLE IF NOT EXISTS promotion_point_activity
(
    id           bigint      NOT NULL AUTO_INCREMENT COMMENT '积分商城活动编号',
    spu_id       bigint      NOT NULL COMMENT '积分商城活动商品',
    status       tinyint     NOT NULL COMMENT '活动状态',
    remark       varchar(500) COMMENT '备注',
    sort         int         NOT NULL DEFAULT 0 COMMENT '排序',
    stock        int         NOT NULL DEFAULT 0 COMMENT '积分商城活动库存',
    total_stock  int         NOT NULL DEFAULT 0 COMMENT '积分商城活动总库存',
    creator      varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater      varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '积分商城活动';

CREATE TABLE IF NOT EXISTS promotion_point_product
(
    id              bigint      NOT NULL AUTO_INCREMENT COMMENT '积分商城商品编号',
    activity_id     bigint      NOT NULL COMMENT '积分商城活动 id',
    spu_id          bigint      NOT NULL COMMENT '商品 SPU 编号',
    sku_id          bigint      NOT NULL COMMENT '商品 SKU 编号',
    count           int         NOT NULL DEFAULT 0 COMMENT '可兑换次数',
    point           int         NOT NULL DEFAULT 0 COMMENT '所需兑换积分',
    price           int         NOT NULL DEFAULT 0 COMMENT '所需兑换金额（分）',
    stock           int         NOT NULL DEFAULT 0 COMMENT '积分商城商品库存',
    activity_status tinyint     NOT NULL COMMENT '积分商城商品状态',
    creator         varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '积分商城商品';


-- 1. product_statistics（商品统计，按天+商品维度）
CREATE TABLE IF NOT EXISTS product_statistics (
                                                  id                     bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
                                                  time                   date         NOT NULL COMMENT '统计日期',
                                                  spu_id                 bigint       NOT NULL COMMENT '商品 SPU 编号',
                                                  browse_count           int          NOT NULL DEFAULT 0 COMMENT '浏览量',
                                                  browse_user_count      int          NOT NULL DEFAULT 0 COMMENT '访客量',
                                                  favorite_count         int          NOT NULL DEFAULT 0 COMMENT '收藏数量',
                                                  cart_count             int          NOT NULL DEFAULT 0 COMMENT '加购数量',
                                                  order_count            int          NOT NULL DEFAULT 0 COMMENT '下单件数',
                                                  order_pay_count        int          NOT NULL DEFAULT 0 COMMENT '支付件数',
                                                  order_pay_price        int          NOT NULL DEFAULT 0 COMMENT '支付金额，单位：分',
                                                  after_sale_count       int          NOT NULL DEFAULT 0 COMMENT '退款件数',
                                                  after_sale_refund_price int         NOT NULL DEFAULT 0 COMMENT '退款金额，单位：分',
                                                  browse_convert_percent int          NOT NULL DEFAULT 0 COMMENT '访客支付转化率（百分比）',
                                                  creator                varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除',
    tenant_id              bigint(20)   NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品统计表';

-- 2. trade_statistics（上一轮已给出，交易统计，按天维度）
CREATE TABLE IF NOT EXISTS trade_statistics (
                                                id                       bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
                                                time                     datetime     NOT NULL COMMENT '统计日期',
                                                order_create_count       int          NOT NULL DEFAULT 0 COMMENT '创建订单数',
                                                order_pay_count          int          NOT NULL DEFAULT 0 COMMENT '支付订单商品数',
                                                order_pay_price          int          NOT NULL DEFAULT 0 COMMENT '总支付金额，单位：分',
                                                after_sale_count         int          NOT NULL DEFAULT 0 COMMENT '退款订单数',
                                                after_sale_refund_price  int          NOT NULL DEFAULT 0 COMMENT '总退款金额，单位：分',
                                                brokerage_settlement_price int        NOT NULL DEFAULT 0 COMMENT '佣金金额（已结算），单位：分',
                                                wallet_pay_price         int          NOT NULL DEFAULT 0 COMMENT '总支付金额（余额），单位：分',
                                                recharge_pay_count       int          NOT NULL DEFAULT 0 COMMENT '充值订单数',
                                                recharge_pay_price       int          NOT NULL DEFAULT 0 COMMENT '充值金额，单位：分',
                                                recharge_refund_count    int          NOT NULL DEFAULT 0 COMMENT '充值退款订单数',
                                                recharge_refund_price    int          NOT NULL DEFAULT 0 COMMENT '充值退款金额，单位：分',
                                                creator                  varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time              datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                  varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time              datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                  tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除',
    tenant_id                bigint(20)   NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易统计表';
