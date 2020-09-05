fun main() {

    val reducer: Reducer<FeatureState, FeatureResult> = DataReducer()
    var newState: FeatureState? = reducer.reduce(FeatureState.idle(), Result.Load.Success("XX", "YY"))
    println("state = $newState")
    newState = try {
        reducer.reduce(newState as FeatureState, Result2.Insert.Success(false))
    } catch (throwable: Throwable) {
        null
    }
    println("state = $newState")

    val reducer2: Reducer<FeatureState, FeatureResult> = DataReducer2()
    newState = reducer2.reduce(FeatureState.idle(), Result.Load.Success("XX", "YY"))
    println("state = $newState")
    newState = reducer2.reduce(newState, Result2.Insert.Success(false))
    println("state = $newState")

}

interface Reducer<state : MVIState, result : MVIResult> {

    fun reduce(previousState: state, result: result): FeatureState

}

open class DataReducer : Reducer<FeatureState, FeatureResult> {
    override fun reduce(previousState: FeatureState, result: FeatureResult): FeatureState {
        return when (result) {
            is Result.Load -> {
                when (result) {
                    is Result.Load.Success -> previousState.copy(
                        name = result.name,
                        address = result.address
                    )
                    is Result.Load.Failed -> previousState.copy()
                }
            }
            else -> {
                throw Throwable("State not Found")
            }
        }
    }
}

class DataReducer2 : DataReducer() {
    override fun reduce(previousState: FeatureState, result: FeatureResult): FeatureState {
        return when (result) {
            is Result2.Insert -> {
                when (result) {
                    is Result2.Insert.Success -> previousState.copy(inserted = result.success)
                    is Result2.Insert.Failed -> previousState
                }
            }
            else -> {
                super.reduce(previousState, result)
            }
        }
    }
}

interface MVIState

data class FeatureState(val name: String?, val address: String?, val throwable: Throwable?, val inserted: Boolean?) :
    MVIState {

    companion object {
        fun idle(): FeatureState {
            return FeatureState(name = null, address = null, throwable = null, inserted = null)
        }
    }
}

interface MVIResult

interface FeatureResult : MVIResult

sealed class Result : FeatureResult {

    sealed class Load : Result() {

        data class Success(val name: String, val address: String) : Load()

        data class Failed(val throwable: Throwable) : Load()
    }
}

sealed class Result2 : FeatureResult {

    sealed class Insert : Result2() {

        data class Success(val success: Boolean) : Insert()

        data class Failed(val throwable: Throwable) : Insert()
    }
}