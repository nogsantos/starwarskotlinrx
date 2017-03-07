package br.eti.fabricionogueira.starwarskotlinrx

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import br.eti.fabricionogueira.starwarskotlinrx.model.api.StarWarsApi
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    var listView : ListView? = null
    var movies = mutableListOf<String>()
    var movieAdapter : ArrayAdapter<String>? = null
    val api = StarWarsApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listView = ListView(this)
        setContentView(listView)
        movieAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, movies)
        listView?.adapter = movieAdapter

        this.listOnly()

//        this.listWithCharacters()
    }
    /**
     * Consulta a lista apenas com os titulos do  filme
     */
    private fun listOnly(){
        api.loadMovies()
            ?.subscribeOn(Schedulers.io()) // Requisição na thread de IO
            ?.observeOn(AndroidSchedulers.mainThread()) // Resultado na main thread do android de UI
            ?.subscribe ( // Listener do resultado
                { // next quando vier cada elemento da lista. Cada item será adicionado na lista
                    movie -> movies.add("${movie.title} -- ${movie.episodeId}")
                },
                { // error, quando houver erros
                    e -> e.printStackTrace()
                },
                { // complete, quando os dados requisitados foram finalizados, totalmente transmitidos
                    movieAdapter?.notifyDataSetChanged() // notifica a mudança
                }
            )
    }
    /**
     *
     */
    private fun listWithCharacters(){
        api.loadMoviesFull()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe (
                        { // next
                            movie -> movies.add("${movie.title} -- ${movie.episodeId}\n ${movie.characters.toString() }")
                        },
                        { // error
                            e -> e.printStackTrace()
                        },
                        { // complete
                            movieAdapter?.notifyDataSetChanged()
                        }
                )
    }


}
