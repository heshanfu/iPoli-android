package io.ipoli.android.store.avatars

import io.ipoli.android.common.BaseRxUseCase
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.store.avatars.data.Avatar
import io.reactivex.Observable
import org.threeten.bp.LocalDate
import timber.log.Timber

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/21/17.
 */
class UseAvatarUseCase(private val playerRepository: PlayerRepository) : BaseRxUseCase<AvatarViewModel, AvatarListPartialStateChange>() {

    override fun createObservable(avatarViewModel: AvatarViewModel): Observable<AvatarListPartialStateChange> =
        playerRepository.findFirst()
            .flatMapSingle { player ->
                player.avatarCode = avatarViewModel.code
                playerRepository.save(player)
            }.map { player ->
            val avatarList = Avatar.values().map {
                AvatarViewModel(it.code, it.avatarName, it.price, it.picture, player.inventory.hasAvatar(it.code))
            }
            AvatarUsedPartialStateChange(avatarList,
                avatarList.indexOfFirst { (code) -> code == avatarViewModel.code })
                as AvatarListPartialStateChange
        }.startWith(AvatarListLoadingPartialStateChange())

}