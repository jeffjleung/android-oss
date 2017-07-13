package com.kickstarter.ui.viewholders;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectStats;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ProjectActivity;
import com.kickstarter.viewmodels.CreatorDashboardHeaderHolderViewModel;

import java.math.RoundingMode;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.R.id.creator_dashboard_time_remaining;
import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class CreatorDashboardHeaderViewHolder extends KSViewHolder {

  private final CreatorDashboardHeaderHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.creator_dashboard_amount_raised) TextView amountRaisedTextView;
  protected @Bind(R.id.creator_dashboard_backer_count) TextView backerCountTextView;
  protected @Bind(R.id.creator_dashboard_funding_text) TextView fundingTextTextView;
  protected @Bind(R.id.creator_dashboard_percent) TextView percentTextView;
  protected @Bind(R.id.creator_dashboard_project_name) TextView projectNameTextView;
  protected @Bind(creator_dashboard_time_remaining) TextView timeRemainingTextView;
  protected @Bind(R.id.creator_dashboard_time_remaining_text) TextView timeRemainingTextTextView;
  protected @BindString(R.string.discovery_baseball_card_stats_pledged_of_goal) String pledgedOfGoalString;

  private KSString ksString;
  private KSCurrency ksCurrency;

  public CreatorDashboardHeaderViewHolder(final @NonNull View view) {
    super(view);

    viewModel = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);

    this.ksCurrency = this.environment().ksCurrency();
    this.ksString = this.environment().ksString();

    viewModel.outputs.currentProject()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setTimeRemainingTextTextView);

    viewModel.outputs.currentProject()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setPledgedOfGoalString);

    viewModel.outputs.currentProject()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setPledgedOfGoalString);

    viewModel.outputs.percentageFunded()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percentTextView::setText);

    viewModel.outputs.projectBackersCountText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(backerCountTextView::setText);

    viewModel.outputs.projectNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(projectNameTextView::setText);

    viewModel.outputs.timeRemainingText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(timeRemainingTextView::setText);

    viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(projectAndRefTag -> this.startProjectActivity(projectAndRefTag.first, projectAndRefTag.second));
  }

  @OnClick({ R.id.creator_view_project_button })
  protected void viewProjectButtonClicked() {
    this.viewModel.inputs.projectViewClicked();
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    // coerce to projectstats and project
    final Pair<Project, ProjectStats> projectAndProjectStats = requireNonNull((Pair<Project, ProjectStats>) data);
    final Project project = requireNonNull(projectAndProjectStats.first, Project.class);
    final ProjectStats projectStats = requireNonNull(projectAndProjectStats.second, ProjectStats.class);
    viewModel.inputs.projectAndStats(project, projectStats);
  }

  private void setPledgedOfGoalString(final @NonNull Project latestProject) {
    final String goalString = ksCurrency.format(latestProject.pledged(), latestProject, false, true, RoundingMode.DOWN);
    amountRaisedTextView.setText(goalString);

    final String goalText = ksString.format(this.pledgedOfGoalString, "goal", goalString);
    fundingTextTextView.setText(goalText);
  }

  private void setTimeRemainingTextTextView(final @NonNull Project latestProject) {
    timeRemainingTextTextView.setText(ProjectUtils.deadlineCountdownDetail(latestProject, this.context(), ksString));
  }

  private void startProjectActivity(final @NonNull Project project, final @NonNull RefTag refTag) {
    final Intent intent = new Intent(this.context(), ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.REF_TAG, refTag);
    this.context().startActivity(intent);
  }

}
