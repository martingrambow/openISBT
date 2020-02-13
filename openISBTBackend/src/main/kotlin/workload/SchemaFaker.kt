package workload

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import java.net.ConnectException

object SchemaFaker {

    var port:Int = 9080
    private val client = HttpClient()

    suspend fun fakeSchema(schema: String) : String {
        var answer: String
        //Call fakerServer
        try {

            answer = client.get<String>("http://localhost:$port") {
                body = schema
            }
        } catch (e: ConnectException) {
            answer = "Error: Connect exception while connecting to fakerServer"
        }
        return answer
    }

    fun close() {
        client.close()
    }
}