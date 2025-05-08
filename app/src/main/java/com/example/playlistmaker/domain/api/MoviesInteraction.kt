import com.example.playlistmaker.domain.models.Movie
import com.example.playlistmaker.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface MoviesInteraction {
    fun searchMovies(query: String): Flow<Resource<List<Movie>>>
}