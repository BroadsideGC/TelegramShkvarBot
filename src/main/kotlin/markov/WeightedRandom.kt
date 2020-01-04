package markov

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Function

class WeightedRandom<T: Int?>(val expr: Expression<T>) : Function<T>(IntegerColumnType()) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder { append("-LOG(1.0 - RANDOM())/", expr) }
}