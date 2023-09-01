package casa.derapps.tola

class NewsData() {

    lateinit var name: String
    lateinit var description: String
    lateinit var url: String

    constructor(name: String, description: String, url: String) : this() {
        this.name = name
        this.description = description
        this.url = url

    }
}