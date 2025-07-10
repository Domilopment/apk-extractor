package domilopment.apkextractor.utils

class AndroidVersion private constructor(
    val api: Int,
    val version: String,
    val codename: String
) {
    private enum class AndroidVersions(val api: Int, val version: String, val codename: String) {
        BASE(1, "1.0", ""),
        BASE_1_1(2, "1.1", "Petit Four"),
        CUPCAKE(3, "1.5", "Cupcake"),
        DONUT(4, "1.6", "Donut"),
        ECLAIR(5, "2.0", "Eclair"),
        ECLAIR_0_1(6, "2.0.1", "Eclair"),
        ECLAIR_MR1(7, "2.1", "Eclair"),
        FROYO(8, "2.2", "Froyo"),
        GINGERBREAD(9, "2.3.0 - 2.3.2", "Gingerbread"),
        GINGERBREAD_MR1(10, "2.3.3 - 2.3.7", "Gingerbread"),
        HONEYCOMB(11, "3.0", "Honeycomb"),
        HONEYCOMB_MR1(12, "3.1", "Honeycomb"),
        HONEYCOMB_MR2(13, "3.2", "Honeycomb"),
        ICE_CREAM_SANDWICH(14, "4.0.1 - 4.0.2", "Ice Cream Sandwich"),
        ICE_CREAM_SANDWICH_MR1(15, "4.0.3 - 4.0.4", "Ice Cream Sandwich"),
        JELLY_BEAN(16, "4.1", "Jelly Bean"),
        JELLY_BEAN_MR1(17, "4.2", "Jelly Bean"),
        JELLY_BEAN_MR2(18, "4.3", "Jelly Bean"),
        KITKAT(19, "4.4", "KitKat Wear"),
        KITKAT_WATCH(20, "4.4W", "KitKat Wear"),
        LOLLIPOP(21, "5.0", "Lollipop"),
        LOLLIPOP_MR1(22, "5.1", "Lollipop"),
        MARSHMALLOW(23, "6.0", "Marshmallow"),
        NOUGAT(24, "7.0", "Nougat"),
        NOUGAT_MR1(25, "7.1", "Nougat"),
        OREO(26, "8.0", "Oreo"),
        OREO_MR1(27, "8.1", "Oreo"),
        PIE(28, "9", "Pie"),
        QUINCE_TART(29, "10", "Quince Tart"),
        RED_VELVET_CAKE(30, "11", "Red Velvet Cake"),
        SNOW_CONE(31, "12", "Snow Cone"),
        SNOW_CONE_V2(32, "12L", "Snow Cone v2"),
        TIRAMISU(33, "13", "Tiramisu"),
        UP_SIDE_DOWN_CAKE(34, "14", "Upside Down Cake"),
        VANILLA_ICE_CREAM(35, "15", "Vanilla Ice Cream"),
        BAKLAVA(36, "16", "Baklava");
    }

    companion object {
        fun fromApi(api: Int?): AndroidVersion {
            return AndroidVersions.entries.find { it.api == api }
                ?.let { AndroidVersion(it.api, it.version, it.codename) }
                ?: AndroidVersion(api ?: -1, "Unknown", "")
        }
    }
}