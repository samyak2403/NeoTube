package com.samyak.neotube.data.recommendation

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test

class NeoTubeNeuroEngineTest {

    private lateinit var engine: NeoTubeNeuroEngine

    @Before
    fun setup() {
        val context: Context = mockk(relaxed = true)
        engine = NeoTubeNeuroEngine.getInstance(context)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getPersona returns INITIATE for new users`() {
        val brain = UserBrain(totalInteractions = 5)
        val persona = engine.getPersona(brain)
        assertThat(persona).isEqualTo(NeoTubePersona.INITIATE)
    }

    @Test
    fun `getPersona returns AUDIOPHILE for music heavy users`() {
        val topics = mapOf("music" to 10.0, "lofi" to 5.0, "remix" to 5.0)
        val brain = UserBrain(
            globalVector = ContentVector(topics = topics),
            totalInteractions = 20
        )
        val persona = engine.getPersona(brain)
        assertThat(persona).isEqualTo(NeoTubePersona.AUDIOPHILE)
    }

    @Test
    fun `getPersona returns SCHOLAR for complex content`() {
        val topics = mapOf("quantum" to 5.0, "physics" to 5.0, "derivative" to 5.0)
        val brain = UserBrain(
            globalVector = ContentVector(topics = topics, complexity = 0.8),
            totalInteractions = 20
        )
        val persona = engine.getPersona(brain)
        assertThat(persona).isEqualTo(NeoTubePersona.SCHOLAR)
    }

    @Test
    fun `getPersona returns DEEP_DIVER for long form content`() {
        val topics = mapOf("essay" to 5.0, "documentary" to 5.0)
        val brain = UserBrain(
            globalVector = ContentVector(topics = topics, duration = 0.8),
            totalInteractions = 20
        )
        val persona = engine.getPersona(brain)
        assertThat(persona).isEqualTo(NeoTubePersona.DEEP_DIVER)
    }
}
