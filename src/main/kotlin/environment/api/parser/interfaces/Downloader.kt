package environment.api.parser.interfaces

interface Downloader<T> {
    fun download() : List<T>
}