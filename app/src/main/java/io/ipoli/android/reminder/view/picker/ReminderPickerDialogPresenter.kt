package io.ipoli.android.reminder.view.picker

import io.ipoli.android.Constants
import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.parser.ReminderMinutesParser
import io.ipoli.android.reminder.view.formatter.ReminderTimeFormatter
import io.ipoli.android.reminder.view.formatter.TimeUnitFormatter
import io.ipoli.android.reminder.view.picker.ReminderPickerViewState.StateType.*
import timber.log.Timber

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/5/17.
 */
class ReminderPickerDialogPresenter(
    private val reminderTimeFormatter: ReminderTimeFormatter,
    private val timeUnitFormatter: TimeUnitFormatter
) : BaseMviPresenter<ReminderPickerDialogController, ReminderPickerViewState>(ReminderPickerViewState.Loading) {

    override fun bindIntents() =
        listOf(
            bindLoadNewReminderIntent(),
            bindEditReminderIntent(),
            bindPickReminderIntent(),
            bindMessageChangeIntent(),
            bindPredefinedValueChangeIntent(),
            bindCustomTimeChangeIntent(),
            bindTimeUnitChangeIntent()
        )

    private fun bindTimeUnitChangeIntent() =
        on { it.timeUnitChangeIntent() }.map { (state, index) ->
            state.copy(type = NEW_VALUES, timeUnitIndex = index)
        }.cast(ReminderPickerViewState::class.java)

    private fun bindPredefinedValueChangeIntent() =
        on { it.predefinedValueChangeIntent() }.map { (state, index) ->
            if (index == reminderTimeFormatter.predefinedTimes.size - 1) {
                state.copy(
                    type = CUSTOM_TIME,
                    timeValue = "",
                    timeUnits = timeUnitFormatter.customTimeUnits,
                    timeUnitIndex = 0,
                    predefinedIndex = null,
                    predefinedValues = listOf()
                )
            } else {
                state.copy(type = NEW_VALUES, predefinedIndex = index)
            }
        }.cast(ReminderPickerViewState::class.java)

    private fun bindMessageChangeIntent() =
        on { it.messageChangeIntent() }.map { (state, text) ->
            state.copy(type = NEW_VALUES, message = text)
        }.cast(ReminderPickerViewState::class.java)

    private fun bindCustomTimeChangeIntent() =
        on { it.customTimeChangeIntent() }.map { (state, text) ->
            state.copy(type = NEW_VALUES, timeValue = text)
        }.cast(ReminderPickerViewState::class.java)

    private fun bindPickReminderIntent() =
        on { it.pickReminderIntent() }.map { (state, _) ->
            if (state.timeUnitIndex != null && state.timeValue.isEmpty()) {
                state.copy(type = TIME_VALUE_VALIDATION_ERROR)
            } else {
                state.copy(
                    type = FINISHED,
                    viewModel = ReminderViewModel(
                        state.message,
                        calculateMinutesFromStart(state))
                )
            }
        }.cast(ReminderPickerViewState::class.java)

    private fun calculateMinutesFromStart(state: ReminderPickerViewState): Long {
        return if (state.timeUnitIndex != null) {
            val timeUnitMinutes = TimeUnit.values()[state.timeUnitIndex].minutes
            state.timeValue.toLong() * timeUnitMinutes
        } else {
            Constants.REMINDER_PREDEFINED_MINUTES[state.predefinedIndex!!].toLong()
        }
    }

    private fun bindEditReminderIntent() =
        on { it.loadReminderData() }.map { (state, reminder) ->
            val (timeValue, timeUnit) = ReminderMinutesParser
                .parseCustomMinutes(reminder.minutesFromStart)

            state.copy(
                type = EDIT_REMINDER,
                message = reminder.message!!,
                timeValue = timeValue.toString(),
                timeUnits = timeUnitFormatter.customTimeUnits,
                timeUnitIndex = timeUnit.ordinal
            )
        }.cast(ReminderPickerViewState::class.java)

    private fun bindLoadNewReminderIntent() =
        on { it.loadNewReminderData() }.map { (state, _) ->
            state.copy(
                type = NEW_REMINDER,
                predefinedValues = reminderTimeFormatter.predefinedTimes,
                predefinedIndex = 0
            )
        }.cast(ReminderPickerViewState::class.java)
}