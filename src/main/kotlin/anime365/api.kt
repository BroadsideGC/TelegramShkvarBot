package anime365


import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

const val baseUrl = "https://smotret-anime.ru/api/"
val client = HttpClient()

@Serializable
data class Episode(val episodeFull: String, val firstUploadedDateTime: String, val isActive: Int)

@Serializable
data class Series(val title: String, val isAiring: Int)

@Serializable
data class Translation(val activeDateTime: String, val episode: Episode, val series: Series)

@Serializable
data class TranslationData(val data: List<Translation>)

suspend fun getTodayOngoingTranslations(): List<Translation> {
    val req = client.get<String>("${baseUrl}translations?feed=recent")
    val data = Json.nonstrict.parse(TranslationData.serializer(), req).data
    return data.filter { LocalDate.now().dayOfYear == LocalDate.parse(it.episode.firstUploadedDateTime.split(' ')[0]).dayOfYear }
        .filter { it.episode.isActive == 1 && it.series.isAiring == 1 }
}
