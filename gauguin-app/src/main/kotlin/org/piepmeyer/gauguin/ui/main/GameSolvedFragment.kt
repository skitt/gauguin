package org.piepmeyer.gauguin.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.piepmeyer.gauguin.R
import org.piepmeyer.gauguin.Utils
import org.piepmeyer.gauguin.databinding.FragmentMainGameSolvedBinding
import org.piepmeyer.gauguin.game.Game
import org.piepmeyer.gauguin.game.GameLifecycle
import org.piepmeyer.gauguin.game.GameSolvedListener
import org.piepmeyer.gauguin.game.GridCreationListener
import org.piepmeyer.gauguin.preferences.StatisticsManager
import org.piepmeyer.gauguin.preferences.TypeOfSolution
import org.piepmeyer.gauguin.ui.statistics.StatisticsActivity

class GameSolvedFragment :
    Fragment(R.layout.fragment_main_game_solved),
    KoinComponent,
    GameSolvedListener,
    GridCreationListener {
    private val game: Game by inject()
    private val gameLifecycle: GameLifecycle by inject()
    private val statisticsManager: StatisticsManager by inject()

    private lateinit var binding: FragmentMainGameSolvedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMainGameSolvedBinding.inflate(inflater, parent, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        binding.showStatisticsButton.setOnClickListener {
            requireActivity().startActivity(
                Intent(
                    requireActivity(),
                    StatisticsActivity::class.java,
                ),
            )
        }

        binding.playGameWithSameConfig.setOnClickListener {
            gameLifecycle.postNewGame(startedFromMainActivityWithSameVariant = true)
        }

        binding.playGameWithOtherConfig.setOnClickListener {
            (this.activity as MainActivity).showNewGameDialog()
        }

        game.addGameSolvedHandler(this)
        game.addGridCreationListener(this)

        freshGridWasCreated()
    }

    override fun onDestroy() {
        super.onDestroy()

        game.removeGridCreationListener(this)
        game.removeGameSolvedHandler(this)
    }

    override fun puzzleSolved(troughReveal: Boolean) {
        this.context?.let {
            if (!troughReveal) {
                val icon =
                    when (statisticsManager.typeOfSolution(game.grid)) {
                        TypeOfSolution.FirstGame -> R.drawable.trophy_variant_outline
                        TypeOfSolution.FirstGameOfKind -> R.drawable.trophy_variant_outline
                        TypeOfSolution.BestTimeOfKind -> R.drawable.podium_gold
                        TypeOfSolution.Regular -> null
                    }

                val text =
                    when (statisticsManager.typeOfSolution(game.grid)) {
                        TypeOfSolution.FirstGame -> it.getString(R.string.puzzle_solved_type_of_solution_first_game_solved)
                        TypeOfSolution.FirstGameOfKind -> it.getString(R.string.puzzle_solved_type_of_solution_first_game_of_kind_solved)
                        TypeOfSolution.BestTimeOfKind -> it.getString(R.string.puzzle_solved_type_of_solution_best_time_of_kind_solved)
                        TypeOfSolution.Regular ->
                            it.getString(
                                R.string.puzzle_solved_type_of_solution_regular_display_best_time,
                                Utils.displayableGameDuration(statisticsManager.getBestTime(game.grid)),
                            )
                    }

                if (icon != null) {
                    binding.detailsIcon.setImageResource(icon)
                    binding.detailsIcon.visibility = View.VISIBLE
                } else {
                    binding.detailsIcon.visibility = View.INVISIBLE
                }

                binding.detailsText.text = text
                binding.detailsText.visibility = View.VISIBLE
            } else {
                binding.detailsIcon.visibility = View.INVISIBLE
                binding.detailsText.visibility = View.INVISIBLE
            }

            binding.gameSolvedCardView.visibility = View.VISIBLE
        }
    }

    override fun freshGridWasCreated() {
        if (!isAdded) {
            return
        }

        requireActivity().runOnUiThread {
            if (game.grid.isSolved()) {
                puzzleSolved(false)
            } else {
                binding.gameSolvedCardView.visibility = View.GONE
            }
        }
    }
}
