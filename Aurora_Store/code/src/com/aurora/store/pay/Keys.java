package com.aurora.store.pay;

/**
 * Created by joy on 2/12/15.
 */
public final class Keys {
    // 请参考 Android平台安全支付服务(msp)应用开发接口(4.2 RSA算法签名)部分，并使用压缩包中的openssl RSA密钥生成工具，生成一套RSA公私钥。
    // 这里签名时，只需要使用生成的RSA私钥。
    // Note: 为安全起见，使用RSA私钥进行签名的操作过程，应该尽量放到商家服务器端去进行。

    //合作身份者id，以2088开头的16位纯数字，这个你申请支付宝签约成功后就会看见
    public static final String DEFAULT_PARTNER = "2088111845670694";
    //这里填写收款支付宝账号，即你付款后到账的支付宝账号
    public static final String DEFAULT_SELLER = "iunipay@iuni.com";
    //商户私钥，自助生成，即rsa_private_key.pem中去掉首行，最后一行，空格和换行最后拼成一行的字符串，
    //rsa_private_key.pem这个文件等你申请支付宝签约成功后，按照文档说明你会生成的................
    //如果android版本太高，这里要用PKCS8格式用户私钥，不然调用不会成功的，那个格式你到时候会生成的。
//    public static final String PRIVATE = "MIICeQIBADANBgkqhkiG9w0BAQEFAASCAmMwggJfAgEAAoGBAMEZyaucvokB+xvf" +
//            "Z4ZAjFbU0KWoYMCee8G6eShG1ek8RAM1iP8wUnsesej4zaz3mZqMI0swah+extD6" +
//            "nYto3dKU7y1nl2Y0m63GMaSS6HYHXCEGsl/AKNyFjYi+3qeEHkS1dNI8jB42ZZsc" +
//            "P9F9iQJdvXgTf4I3oFvK3VnmnOM1AgMBAAECgYEAtdenBqjw9OycoD7dqpuWjR6T" +
//            "7ayGkF2uhV0pQziRsLZEBhGgFVBYHAFn4NCMP1pbmCJoCiJpjaVj7OBVBDeYy1QF" +
//            "ekM64+N3409wGwtC0+x7V8JCThkJRp/gWdfwn/UONtdaxiXAQFSmDsBg0HvUl4a7" +
//            "t9hjwdCgyoXpN5wZ/PECQQD7CEsrKGX8jCMUpexUAXaseOFt1VRngLXQkpJnu6y7" +
//            "/URK/xTpy3/mC8RCqLFnUbeSY4WlUX1PFwDBLsecbfgzAkEAxOwEj+/gJzh6yNOz" +
//            "4nW1tJZ5wzKxAUCI5PgRbyKdcazGMAQHBfh9+NnIntKjQvGkHNJlKh7YRh84IiPJ" +
//            "3jHu9wJBALyX2oEt9JDrZ1+X2Bi0kMe6eQHfB25LEE0GEN/F61vARpJhWQDcVdYq" +
//            "eOPrgHwytEtSYZF1y0Kj8Xkw/93R8GkCQQCBDcR3qFcYnRUq5EMxmL76mNs3p9ME" +
//            "5QxSMpkKYn8Yboh8E0efDBlSqzeljpncV4ycLu6Jyikm23LoPNghPExFAkEAuXIV" +
//            "YR/QfR+0rfwDg6mskNAtl7/vihAch0w/fnqHGYbeAsoFrpE1JyiP56oSMSJIvfF3" +
//            "Dh9wiQPZnErRbTO7xg==";

    public static final String PRIVATE = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBANySoXmSfgx96+Uz" +
            "MXhKoUgq4Hb7MSISp4SFIcFwfuQ6Ii9I6znXjc9WCOyKC8oDUPbb+z6JHWwalYlC" +
            "Vg/GmeJOmjLpcHRcDd7n+T7N/cHtqNZMcYSBmpHdhNxRoK2Yz8pKTELzMHPS8BoQ" +
            "dCJgk0mYi94thEqbebfi5LTubNVrAgMBAAECgYEAm8Hzwwy+dAXLubtv2Jki0Ppu" +
            "LbbuGDiCEOOOVbIh+PpQP4HHkb30hxAjFx3Ye1viJaBsB9n9a+RN6nJsoZFGAihl" +
            "//7F4Vy0tXvwuS3O860CbVX2zgt95hg7plTavhaop6C5Jkw1GIpolP4WEOl+k9tn" +
            "V2tLDncaoVkrM9XIGHkCQQD1XucdVcxeyO9aW53kjn8XlQqLrL++5wxJHH/q+Iu9" +
            "7bBqRN5qCtKO9oCpSDhOiBi9BsLi/8A5ptN0oVzATma3AkEA5iC5rffiJsz2w0ym" +
            "W4vwqd4gMn+455CJSm22ZJT8ymZ3fEQ5blaeqbDLdJMOzDK8GzMLmYpd4m88oPPE" +
            "EHgy7QJBANvXIb74xlkOMtWETF0hBuG8GWy3ZDzIigtfS4TdF2cd9PfhqFzeQXTx" +
            "iqkhGcHS0kdaLXZwqmt8+uv+PznVhNUCQQCxIi1qICx+KzQhncVLIAst5WEpHbCo" +
            "5VMX7B0BjMCL1pVQHH+MU7Yq7X+dinhQaxjpYsAh/iRH39FuHlQjTTH9AkBr3XVd" +
            "tFnT2RCJpcPUf2o4gP6BoCYzJSKBFZfn6CI3hZAVDeGv6oqqzVUIMB4bh6Ju3RK7" +
            "QwEmLrp0FmyY1JyA";

    //支付宝（RSA）公钥  ,demo自带不用改，或者用签约支付宝账号登录ms.alipay.com后，在密钥管理页面获取；或者文档上也有。
    public static final String PUBLIC = "";
}
