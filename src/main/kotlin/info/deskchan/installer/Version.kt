package info.deskchan.installer


interface Version {
    val major: Int
    val minor: Int
    val patch: Int

    operator fun compareTo(another: Version): Int {
        listOf(Pair(major, another.major), Pair(minor, another.minor), Pair(patch, another.patch)).forEach {
            val comparisonResult = it.first - it.second
            if (comparisonResult != 0) {
                return comparisonResult
            }
        }
        return 0
    }

    override fun toString(): String
}


data class SemanticVersion(override val major: Int, override val minor: Int, override val patch: Int, val specifier: String? = null): Version {

    companion object {
        fun fromString(version: String): SemanticVersion? {
            val expr = "v([0-9]+)\\.([0-9]+)\\.([0-9]+)(-(.+))?".toRegex()
            val matches = expr.matchEntire(version)?.groups

            if (matches != null && matches.size >= 4) {
                return try {
                    val major = matches[1]!!.value.toInt()
                    val minor = matches[2]!!.value.toInt()
                    val patch = matches[3]!!.value.toInt()
                    val specifier = if (matches.size >= 6) matches[5]!!.value else null
                    SemanticVersion(major, minor, patch, specifier)
                } catch (e: Exception) {
                    view.log(e)
                    null
                }

            }
            return null
        }
    }

    override fun toString(): String {
        val version = "v$major.$minor.$patch"
        return specifier?.let { "$version-$it" } ?: version
    }

}


data class CommitVersion(override val major: Int, override val minor: Int, override val patch: Int, val commitNumber: Int): Version {

    companion object {
        fun fromString(version: String): Version? {
            val expr = "v([0-9]+)\\.([0-9]+)\\.([0-9]+)-r([0-9]+)".toRegex()
            val matches = expr.matchEntire(version)?.groups

            if (matches != null && matches.size >= 5) {
                return try {
                    val major = matches[1]!!.value.toInt()
                    val minor = matches[2]!!.value.toInt()
                    val patch = matches[3]!!.value.toInt()
                    val commitNumber = matches[4]!!.value.toInt()
                    CommitVersion(major, minor, patch, commitNumber)
                } catch (e: Exception) {
                    view.log(e)
                    null
                }

            }
            return null
        }
    }

    override operator fun compareTo(another: Version): Int {
        val comparisonResult = super.compareTo(another)
        if (comparisonResult != 0 || another !is CommitVersion) {
            return comparisonResult
        }
        return this.commitNumber - another.commitNumber
    }

    override fun toString() = "v$major.$minor.$patch-r$commitNumber"

}


fun parseVersion(version: String): Version? {
    listOf(SemanticVersion.Companion::fromString, CommitVersion.Companion::fromString).forEach {
        val obj = it(version)
        obj?.let { return obj }
    }
    return null
}
