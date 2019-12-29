package environment.api.parser.interfaces

interface Uploader<T> {
    fun uploadSingleItem(item : T)
}