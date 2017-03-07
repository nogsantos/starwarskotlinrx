package br.eti.fabricionogueira.starwarskotlinrx.model

/**
 * Created by nogsantos on 06/03/17.
 */

data class Movie (val title : String,
                  val episodeId : Int,
                  val characters : MutableList<Character>)

data class Character(val name : String,
                     val gender : String){

    override fun toString(): String {
        return "${name} / ${gender}"
    }
}