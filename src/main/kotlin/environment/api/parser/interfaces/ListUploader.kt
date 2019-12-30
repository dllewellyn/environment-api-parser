package environment.api.parser.interfaces

interface ListUploader<T> {
    fun upload(name : String, items : List<T>)
}