package mypoli.android.timer.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.common.datetime.minutes
import mypoli.android.quest.Quest
import mypoli.android.quest.TimeRange
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.timer.job.TimerCompleteScheduler
import org.threeten.bp.Instant

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/18/18.
 */
class CompleteTimeRangeUseCase(
    private val questRepository: QuestRepository,
    private val splitDurationForPomodoroTimerUseCase: SplitDurationForPomodoroTimerUseCase,
    private val timerCompleteScheduler: TimerCompleteScheduler
) :
    UseCase<CompleteTimeRangeUseCase.Params, Quest> {

    override fun execute(parameters: Params): Quest {
        val quest = questRepository.findById(parameters.questId)
        requireNotNull(quest)
        require(quest!!.pomodoroTimeRanges.isNotEmpty())

        val time = parameters.time

        val splitResult = splitDurationForPomodoroTimerUseCase
            .execute(SplitDurationForPomodoroTimerUseCase.Params(quest))

        if (splitResult == SplitDurationForPomodoroTimerUseCase.Result.DurationNotSplit) {
            return questRepository.save(endLastTimeRange(quest, time))
        }

        val timeRanges =
            (splitResult as SplitDurationForPomodoroTimerUseCase.Result.DurationSplit).timeRanges

        if (timeRanges.size <= quest.pomodoroTimeRanges.size) {
            return questRepository.save(endLastTimeRange(quest, time))
        }

        val questWithEndedLastRange = endLastTimeRange(quest, time)
        val currentTimeRanges = questWithEndedLastRange.pomodoroTimeRanges.toMutableList()
        val lastRangeType = questWithEndedLastRange.pomodoroTimeRanges.last().type

        val newRangeDuration: Int
        val newRangeType = if (lastRangeType == TimeRange.Type.BREAK) {
            newRangeDuration = Constants.DEFAULT_POMODORO_WORK_DURATION
            TimeRange.Type.WORK
        } else {
            newRangeDuration = if ((currentTimeRanges.size + 1) % 8 == 0) {
                Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION
            } else {
                Constants.DEFAULT_POMODORO_BREAK_DURATION
            }
            TimeRange.Type.BREAK
        }

        val newQuest = questWithEndedLastRange.copy(
            pomodoroTimeRanges = currentTimeRanges +
                TimeRange(
                    newRangeType,
                    newRangeDuration,
                    start = time
                )
        )

        timerCompleteScheduler.schedule(
            questId = quest.id,
            after = newRangeDuration.minutes
        )

        return questRepository.save(newQuest)
    }

    private fun endLastTimeRange(
        quest: Quest,
        time: Instant
    ): Quest {
        val lastTimeRange = quest.pomodoroTimeRanges.last().copy(
            end = time
        )
        return quest.copy(
            pomodoroTimeRanges = quest.pomodoroTimeRanges - quest.pomodoroTimeRanges.last() + lastTimeRange
        )
    }

    data class Params(
        val questId: String,
        val time: Instant = Instant.now()
    )
}