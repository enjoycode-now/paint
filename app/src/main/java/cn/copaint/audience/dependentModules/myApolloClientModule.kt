import cn.copaint.audience.apollo.myApolloClient
import org.koin.dsl.module.module

val myApolloClientModules = module {
    single { myApolloClient }
}