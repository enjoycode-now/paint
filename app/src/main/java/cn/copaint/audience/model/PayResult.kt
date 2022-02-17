package cn.copaint.audience.model

/**
 * author：chen
 * date: 2022/2/17
 * desc:解析支付宝支付返回string
 * 返回例子： resultStatus={9000};memo={};result={{"alipay_trade_app_pay_response":{"code":"10000","msg":"Success","app_id":"2016091300503896","auth_app_id":"2016091300503896","charset":"utf-8","timestamp":"2018-08-28 17:51:11","out_trade_no":"nVElbd74TW6WnEyxQwvX8A","total_amount":"0.01","trade_no":"2018082821001004680500208879","seller_id":"2088102175487650"},"sign":"W0Hg9k4GxL8Oaxymvqk2i65WNDQxYp6HGve32ek6VjSRnymmI3GQTjpQVbZuDzvjcwQ/HIkM97PoBGAVlTmi/wiJcqDgSSDzDY7AFnNN0OcK0ehWGwKQINA4IDGh51A7yY/vYKmR0VW+2OwGhlRPPMMZtQOEqh8a9/aIijzT6ZLwy9Hl4ayG/fVKhdC1VdckF6+C25BFNp3fIxarg5tfEunm7N9iWngKCUsnP+IZz05OHdvynimgYPcBnbBERHG97GVqRT/EdBWTQyIDMc0LemScAYxJixTVgXDkRddQjzWZ7HgLdBfjs0nXY24puHudT76ERxVY+8NkoKle/QI+FA==","sign_type":"RSA2"}}
 *
 */
class PayResult {

    var resultStatus: String? = null

    var result: String? = null

    var memo: String? = null

    constructor(map: Map<*, *>) {
        resultStatus = map["resultStatus"].toString()
        result = map["result"].toString()
        memo = map["memo"].toString()
    }
}
