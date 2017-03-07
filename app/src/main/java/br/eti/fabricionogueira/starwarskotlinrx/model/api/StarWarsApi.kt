package br.eti.fabricionogueira.starwarskotlinrx.model.api

import android.net.Uri
import br.eti.fabricionogueira.starwarskotlinrx.model.Movie
import br.eti.fabricionogueira.starwarskotlinrx.model.Character
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import java.util.*

class StarWarsApi {
    val service: StarWarsApiDef
    var peopleCache = mutableMapOf<String, Person>()
    /**
     * Inicializa a api
     */
    init {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)

        val gson = GsonBuilder().setLenient().create()

        val retrofit = Retrofit.Builder()
                .baseUrl("http://swapi.co/api/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build()

        service = retrofit.create<StarWarsApiDef>(StarWarsApiDef::class.java)
    }
    /**
     * Retorna apenas a lista sem os personagens
     */
    fun loadMovies(): Observable<Movie>? {
        return service.listMovies()
                .flatMap {
                    filmResults -> Observable.from(filmResults.results)
                }
                .map {
                    film -> Movie(film.title, film.episodeId, ArrayList<Character>())
                }
    }
    /**
     * Retorna a lista com os personagens
     */
    fun loadMoviesFull(): Observable<Movie> {
        return service.listMovies()
                .flatMap { filmResults -> Observable.from(filmResults.results) }
                .flatMap {
                    film -> val movieObj = Movie(film.title, film.episodeId, ArrayList<Character>())
                        Observable.zip(
                            Observable.just(movieObj),
                            Observable.from(film.personUrls)
                                .flatMap {
                                    personUrl -> Observable.concat(
                                            getCache(personUrl),
                                            service.loadPerson(Uri.parse(personUrl).lastPathSegment)
                                                    .doOnNext {
                                                        person -> peopleCache.put(personUrl, person)
                                                    }
                                        )
                                        .first()
                                    }
                                    .map {
                                        person -> Character(person!!.name, person.gender)
                                    }
                                    .toList(), {
                                        movie, characters ->
                                            movie.characters.addAll(characters)
                                            movie
                                    }
                    )
                }
    }
    /**
     * Armazena personagens no cache
     */
    private fun getCache(personUrl : String) : Observable<Person?>? {
        return Observable.from(peopleCache.keys)
                .filter { key ->
                    key == personUrl
                }
                .map { key ->
                    peopleCache[key]
                }
    }

}