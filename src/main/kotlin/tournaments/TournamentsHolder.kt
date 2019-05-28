package tournaments

import java.util.concurrent.ConcurrentHashMap

object TournamentsHolder {
    private val ongoing = ConcurrentHashMap<Int, Pair<String, Bracket>>()
    private val byName = ConcurrentHashMap<String, Int>()
    private val currentBracket = ConcurrentHashMap<Int, Bracket>()

    fun put(id: Int, name: String, tournament: Bracket) {
        ongoing[id] = Pair(name, tournament)
        byName[name] = id
    }

    operator fun get(id: Int) = ongoing[id]
    operator fun get(name: String) = ongoing[byName[name]]

    fun next(id: Int) = ongoing[id]?.second?.let {
        val a = it.flatten()
        if (a.isEmpty()) return null
        val c = currentBracket[id]!!
        val fst = a.first()
        val i = a.iterator()
        while (i.hasNext() && i.next().name != c.name);
        val found = if (i.hasNext()) i.next() else fst
        currentBracket[id] = found
        return@let found
    }

    fun current(id: Int) = currentBracket[id]
}