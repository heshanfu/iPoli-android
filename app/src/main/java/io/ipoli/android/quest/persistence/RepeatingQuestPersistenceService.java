package io.ipoli.android.quest.persistence;

import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/31/16.
 */
public interface RepeatingQuestPersistenceService extends PersistenceService<RepeatingQuest> {

    List<RepeatingQuest> findAllNonAllDayActiveRepeatingQuests();

    void findAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener);

    void findNonFlexibleNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener);

    RepeatingQuest findByExternalSourceMappingId(String source, String sourceId);

    void findAllForChallenge(Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener);

    void findActiveForChallenge(Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener);

    List<RepeatingQuest> findActiveNotForChallenge(String query, Challenge challenge);

    void findByChallenge(Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener);
}
