CREATE TABLE IF NOT EXISTS trade_order
(
    id                      bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    no                      varchar(64) NOT NULL COMMENT '订单编号',
    type                    int         NOT NULL COMMENT '订单类型',
    terminal                int         NOT NULL COMMENT '终端类型',
    user_id                 bigint      NOT NULL COMMENT '用户编号',
    user_ip                 varchar(64) NOT NULL COMMENT '用户IP',
    user_remark             varchar(500) COMMENT '用户备注',
    status                  int         NOT NULL COMMENT '订单状态',
    product_count           int         NOT NULL COMMENT '商品数量',
    cancel_type             int COMMENT '取消类型',
    remark                  varchar(500) COMMENT '管理员备注',
    comment_status          tinyint(1) COMMENT '是否评价',
    brokerage_user_id       bigint COMMENT '分销用户ID',
    pay_status              tinyint(1)  NOT NULL COMMENT '支付状态',
    pay_time                datetime COMMENT '支付时间',
    finish_time             datetime COMMENT '完成时间',
    cancel_time             datetime COMMENT '取消时间',
    total_price             int COMMENT '商品总价',
    order_price             int COMMENT '订单原价',
    discount_price          int         NOT NULL COMMENT '优惠金额',
    delivery_price          int         NOT NULL COMMENT '运费金额',
    adjust_price            int         NOT NULL COMMENT '调价金额',
    pay_price               int         NOT NULL COMMENT '支付金额',
    delivery_type           int         NOT NULL COMMENT '配送方式',
    pay_order_id            bigint COMMENT '支付订单ID',
    pay_channel_code        varchar(64) COMMENT '支付渠道',
    delivery_template_id    bigint COMMENT '运费模板ID',
    logistics_id            bigint COMMENT '物流公司ID',
    logistics_no            varchar(64) COMMENT '物流单号',
    delivery_time           datetime COMMENT '发货时间',
    receive_time            datetime COMMENT '收货时间',
    receiver_name           varchar(32) NOT NULL COMMENT '收件人名称',
    receiver_mobile         varchar(20) NOT NULL COMMENT '收件人手机',
    receiver_area_id        int         NOT NULL COMMENT '收件地区ID',
    receiver_post_code      int COMMENT '收件邮编',
    receiver_detail_address varchar(500) NOT NULL COMMENT '收件详细地址',
    pick_up_store_id        bigint COMMENT '自提门店ID',
    pick_up_verify_code     varchar(32) COMMENT '自提核销码',
    refund_status           int COMMENT '退款状态',
    refund_price            int COMMENT '退款金额',
    after_sale_status       int COMMENT '售后状态',
    coupon_id               bigint      NOT NULL COMMENT '优惠券ID',
    coupon_price            int         NOT NULL COMMENT '优惠券抵扣金额',
    use_point               int COMMENT '使用积分',
    point_price             int         NOT NULL COMMENT '积分抵扣金额',
    give_point              int COMMENT '赠送积分',
    refund_point            int COMMENT '退还积分',
    vip_price               int COMMENT '会员优惠金额',
    give_coupons_map        varchar(500) COMMENT '赠送优惠券',
    seckill_activity_id     bigint COMMENT '秒杀活动ID',
    bargain_activity_id     bigint COMMENT '砍价活动ID',
    bargain_record_id       bigint COMMENT '砍价记录ID',
    combination_activity_id bigint COMMENT '拼团活动ID',
    combination_head_id     bigint COMMENT '拼团团长ID',
    combination_record_id   bigint COMMENT '拼团记录ID',
    creator                 varchar(64) DEFAULT '' COMMENT '创建者',
    create_time             datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                 varchar(64) DEFAULT '' COMMENT '更新者',
    update_time             datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                 tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '交易订单表';

CREATE TABLE IF NOT EXISTS trade_order_item
(
    id                bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id           bigint      NOT NULL COMMENT '用户ID',
    order_id          bigint      NOT NULL COMMENT '订单ID',
    cart_id           int COMMENT '购物车ID',
    spu_id            bigint      NOT NULL COMMENT '商品SPU ID',
    spu_name          varchar(255) NOT NULL COMMENT '商品名称',
    sku_id            bigint      NOT NULL COMMENT '商品SKU ID',
    properties        varchar(500) COMMENT '商品属性',
    pic_url           varchar(500) COMMENT '商品图片',
    count             int         NOT NULL COMMENT '购买数量',
    comment_status    tinyint(1) COMMENT '是否评价',
    price             int         NOT NULL COMMENT '商品单价',
    discount_price    int         NOT NULL COMMENT '优惠金额',
    delivery_price    int COMMENT '运费金额',
    adjust_price      int COMMENT '调价金额',
    pay_price         int         NOT NULL COMMENT '实付单价',
    coupon_price      int COMMENT '优惠券分摊金额',
    point_price       int COMMENT '积分分摊金额',
    use_point         int COMMENT '使用积分',
    give_point        int COMMENT '赠送积分',
    vip_price         int COMMENT '会员优惠金额',
    after_sale_id     bigint COMMENT '售后单ID',
    after_sale_status int         NOT NULL COMMENT '售后状态',
    creator           varchar(64) DEFAULT '' COMMENT '创建者',
    create_time       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater           varchar(64) DEFAULT '' COMMENT '更新者',
    update_time       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '交易订单明细表';

CREATE TABLE IF NOT EXISTS trade_after_sale
(
    id                bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    no                varchar(64) NOT NULL COMMENT '售后单号',
    status            int         NOT NULL COMMENT '售后状态',
    type              int         NOT NULL COMMENT '售后类型',
    way               int         NOT NULL COMMENT '处理方式',
    user_id           bigint      NOT NULL COMMENT '用户ID',
    apply_reason      varchar(255) NOT NULL COMMENT '申请原因',
    apply_description varchar(500) COMMENT '申请描述',
    apply_pic_urls    varchar(500) COMMENT '凭证图片',
    order_id          bigint      NOT NULL COMMENT '订单ID',
    order_no          varchar(64) NOT NULL COMMENT '订单编号',
    order_item_id     bigint      NOT NULL COMMENT '订单项ID',
    spu_id            bigint      NOT NULL COMMENT '商品SPU ID',
    spu_name          varchar(255) NOT NULL COMMENT '商品名称',
    sku_id            bigint      NOT NULL COMMENT '商品SKU ID',
    properties        varchar(500) COMMENT '商品属性',
    pic_url           varchar(500) COMMENT '商品图片',
    count             int         NOT NULL COMMENT '售后数量',
    audit_time        varchar(64) COMMENT '审核时间',
    audit_user_id     bigint COMMENT '审核人ID',
    audit_reason      varchar(500) COMMENT '审核备注',
    refund_price      int         NOT NULL COMMENT '退款金额',
    pay_refund_id     bigint COMMENT '支付退款ID',
    refund_time       varchar(64) COMMENT '退款时间',
    logistics_id      bigint COMMENT '物流公司ID',
    logistics_no      varchar(64) COMMENT '物流单号',
    delivery_time     varchar(64) COMMENT '发货时间',
    receive_time      varchar(64) COMMENT '收货时间',
    receive_reason    varchar(500) COMMENT '收货备注',
    creator           varchar(64) DEFAULT '' COMMENT '创建者',
    create_time       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater           varchar(64) DEFAULT '' COMMENT '更新者',
    update_time       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '交易售后表';

CREATE TABLE IF NOT EXISTS trade_after_sale_log
(
    id            bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id       bigint      NOT NULL COMMENT '用户ID',
    user_type     int         NOT NULL COMMENT '用户类型',
    after_sale_id bigint      NOT NULL COMMENT '售后单ID',
    order_id      bigint      NOT NULL COMMENT '订单ID',
    order_item_id bigint      NOT NULL COMMENT '订单项ID',
    before_status int COMMENT '变更前状态',
    after_status  int         NOT NULL COMMENT '变更后状态',
    content       varchar(500) NOT NULL COMMENT '日志内容',
    creator       varchar(64) DEFAULT '' COMMENT '创建者',
    create_time   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater       varchar(64) DEFAULT '' COMMENT '更新者',
    update_time   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '交易售后日志';

CREATE TABLE IF NOT EXISTS trade_brokerage_user
(
    id                bigint     NOT NULL AUTO_INCREMENT COMMENT '主键',
    bind_user_id      bigint     NOT NULL COMMENT '绑定用户ID',
    bind_user_time    varchar(64) COMMENT '绑定时间',
    brokerage_enabled tinyint(1) NOT NULL COMMENT '分销开关',
    brokerage_time    varchar(64) COMMENT '开通时间',
    price             int        NOT NULL COMMENT '佣金金额',
    frozen_price      int        NOT NULL COMMENT '冻结佣金',
    creator           varchar(64) DEFAULT '' COMMENT '创建者',
    create_time       datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater           varchar(64) DEFAULT '' COMMENT '更新者',
    update_time       datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted           tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    tenant_id         bigint     NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '分销用户';

CREATE TABLE IF NOT EXISTS trade_brokerage_record
(
    id               bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id          bigint      NOT NULL COMMENT '用户ID',
    biz_id           varchar(64) NOT NULL COMMENT '业务编号',
    biz_type         varchar(32) NOT NULL COMMENT '业务类型',
    title            varchar(255) NOT NULL COMMENT '记录标题',
    price            int         NOT NULL COMMENT '佣金金额',
    total_price      int         NOT NULL COMMENT '总金额',
    description      varchar(500) NOT NULL COMMENT '描述',
    status           varchar(32) NOT NULL COMMENT '状态',
    frozen_days      int         NOT NULL COMMENT '冻结天数',
    unfreeze_time    varchar(64) COMMENT '解冻时间',
    source_user_level int COMMENT '来源用户等级',
    source_user_id   bigint COMMENT '来源用户ID',
    creator          varchar(64) DEFAULT '' COMMENT '创建者',
    create_time      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater          varchar(64) DEFAULT '' COMMENT '更新者',
    update_time      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    tenant_id        bigint      NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '佣金记录';

CREATE TABLE IF NOT EXISTS trade_brokerage_withdraw
(
    id                  int         NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id             bigint      NOT NULL COMMENT '用户ID',
    price               int         NOT NULL COMMENT '提现金额',
    fee_price           int         NOT NULL COMMENT '手续费',
    total_price         int         NOT NULL COMMENT '实际到账',
    type                varchar(32) NOT NULL COMMENT '提现方式',
    name                varchar(64) COMMENT '开户人',
    account_no          varchar(64) COMMENT '账号',
    bank_name           varchar(100) COMMENT '银行名称',
    bank_address        varchar(255) COMMENT '银行地址',
    account_qr_code_url varchar(500) COMMENT '收款码',
    status              varchar(32) NOT NULL COMMENT '提现状态',
    audit_reason        varchar(500) COMMENT '审核原因',
    audit_time          varchar(64) COMMENT '审核时间',
    remark              varchar(500) COMMENT '备注',
    creator             varchar(64) DEFAULT '' COMMENT '创建者',
    create_time         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater             varchar(64) DEFAULT '' COMMENT '更新者',
    update_time         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    tenant_id           bigint      NOT NULL DEFAULT 0 COMMENT '租户ID',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '佣金提现';

CREATE TABLE IF NOT EXISTS trade_delivery_express
(
    id          int         NOT NULL AUTO_INCREMENT COMMENT '主键',
    code        varchar(32) COMMENT '快递公司编码',
    name        varchar(64) NOT NULL COMMENT '快递公司名称',
    logo        varchar(500) COMMENT 'LOGO',
    sort        int         NOT NULL COMMENT '排序',
    status      int         NOT NULL COMMENT '状态',
    creator     varchar(64) DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64) DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '快递公司';

-- ==============================
-- 交易中心 补充表（7张）
-- ==============================

CREATE TABLE IF NOT EXISTS trade_cart
(
    id          bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    user_id     bigint      NOT NULL COMMENT '用户编号',
    spu_id      bigint      NOT NULL COMMENT '商品 SPU 编号',
    sku_id      bigint      NOT NULL COMMENT '商品 SKU 编号',
    count       int         NOT NULL DEFAULT 0 COMMENT '商品购买数量',
    selected    bit(1)      NOT NULL DEFAULT b'1' COMMENT '是否选中',
    creator     varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater     varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '购物车';

CREATE TABLE IF NOT EXISTS trade_config
(
    id                              bigint      NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    after_sale_refund_reasons       varchar(2000) COMMENT '退款理由（JSON）',
    after_sale_return_reasons       varchar(2000) COMMENT '退货理由（JSON）',
    delivery_express_free_enabled   bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否启用全场包邮',
    delivery_express_free_price     int         NOT NULL DEFAULT 0 COMMENT '全场包邮最小金额（分）',
    delivery_pick_up_enabled        bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否开启自提',
    brokerage_enabled               bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否启用分佣',
    brokerage_enabled_condition     tinyint     COMMENT '分佣模式',
    brokerage_bind_mode             tinyint     COMMENT '分销关系绑定模式',
    brokerage_poster_urls           varchar(2000) COMMENT '分销海报图地址数组（JSON）',
    brokerage_first_percent         int         COMMENT '一级返佣比例',
    brokerage_second_percent        int         COMMENT '二级返佣比例',
    brokerage_withdraw_min_price    int         COMMENT '用户提现最低金额',
    brokerage_withdraw_fee_percent  int         COMMENT '用户提现手续费百分比',
    brokerage_frozen_days           int         COMMENT '佣金冻结时间（天）',
    brokerage_withdraw_types        varchar(256) COMMENT '提现方式（JSON）',
    creator                         varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time                     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater                         varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time                     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                         tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '交易中心配置';

CREATE TABLE IF NOT EXISTS trade_delivery_express_template
(
    id           bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    name         varchar(128) NOT NULL COMMENT '模板名称',
    charge_mode  tinyint     NOT NULL COMMENT '配送计费方式',
    sort         int         NOT NULL DEFAULT 0 COMMENT '排序',
    creator      varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater      varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '快递运费模板';

CREATE TABLE IF NOT EXISTS trade_delivery_express_template_charge
(
    id           bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    template_id  bigint      NOT NULL COMMENT '配送模板编号',
    area_ids     varchar(500) COMMENT '配送区域编号列表（JSON）',
    charge_mode  tinyint     NOT NULL COMMENT '配送计费方式',
    start_count  double      NOT NULL DEFAULT 0 COMMENT '首件数量',
    start_price  int         NOT NULL DEFAULT 0 COMMENT '起步价（分）',
    extra_count  double      NOT NULL DEFAULT 0 COMMENT '续件数量',
    extra_price  int         NOT NULL DEFAULT 0 COMMENT '额外价（分）',
    creator      varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater      varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '快递运费模板计费';

CREATE TABLE IF NOT EXISTS trade_delivery_express_template_free
(
    id           bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    template_id  bigint      NOT NULL COMMENT '配送模板编号',
    area_ids     varchar(500) COMMENT '配送区域编号列表（JSON）',
    free_price   int         NOT NULL DEFAULT 0 COMMENT '包邮金额（分）',
    free_count   int         NOT NULL DEFAULT 0 COMMENT '包邮件数',
    creator      varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater      varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '快递运费包邮区域';

CREATE TABLE IF NOT EXISTS trade_delivery_pick_up_store
(
    id               bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    name             varchar(128) NOT NULL COMMENT '门店名称',
    introduction     varchar(500) COMMENT '门店简介',
    phone            varchar(20) COMMENT '门店手机',
    area_id          int         COMMENT '区域编号',
    detail_address   varchar(256) COMMENT '门店详细地址',
    logo             varchar(512) COMMENT '门店 logo',
    opening_time     time COMMENT '营业开始时间',
    closing_time     time COMMENT '营业结束时间',
    latitude         double COMMENT '纬度',
    longitude        double COMMENT '经度',
    verify_user_ids  varchar(500) COMMENT '核销员工用户编号数组（JSON）',
    status           tinyint     NOT NULL DEFAULT 0 COMMENT '门店状态',
    creator          varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater          varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '自提门店';

CREATE TABLE IF NOT EXISTS trade_order_log
(
    id            bigint      NOT NULL AUTO_INCREMENT COMMENT '编号',
    user_id       bigint      NOT NULL COMMENT '用户编号',
    user_type     tinyint     NOT NULL COMMENT '用户类型',
    order_id      bigint      NOT NULL COMMENT '订单号',
    before_status tinyint COMMENT '操作前状态',
    after_status  tinyint COMMENT '操作后状态',
    operate_type  tinyint     COMMENT '操作类型',
    content       varchar(500) NOT NULL COMMENT '订单日志信息',
    creator       varchar(64)          DEFAULT '' COMMENT '创建者',
    create_time   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater       varchar(64)          DEFAULT '' COMMENT '更新者',
    update_time   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted       tinyint(1)  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id)



    CREATE TABLE IF NOT EXISTS trade_statistics
(
       id                    bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
       time                  datetime     NOT NULL COMMENT '统计日期',
       order_create_count    int          NOT NULL DEFAULT 0 COMMENT '创建订单数',
       order_pay_count       int          NOT NULL DEFAULT 0 COMMENT '支付订单商品数',
       order_pay_price       int          NOT NULL DEFAULT 0 COMMENT '总支付金额，单位：分',
       after_sale_count      int          NOT NULL DEFAULT 0 COMMENT '退款订单数',
       after_sale_refund_price int        NOT NULL DEFAULT 0 COMMENT '总退款金额，单位：分',
       brokerage_settlement_price int     NOT NULL DEFAULT 0 COMMENT '佣金金额（已结算），单位：分',
       wallet_pay_price      int          NOT NULL DEFAULT 0 COMMENT '总支付金额（余额），单位：分',
       recharge_pay_count    int          NOT NULL DEFAULT 0 COMMENT '充值订单数',
       recharge_pay_price    int          NOT NULL DEFAULT 0 COMMENT '充值金额，单位：分',
       recharge_refund_count int          NOT NULL DEFAULT 0 COMMENT '充值退款订单数',
       recharge_refund_price int          NOT NULL DEFAULT 0 COMMENT '充值退款金额，单位：分',
       creator               varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater               varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time           datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted               tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否删除',
    tenant_id             bigint(20)   NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易统计表';
