package mdc.ida.album.model;

import javafx.beans.Observable;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.Map;

public class SolutionBundle {
	private final String group;
	private final String name;
	private final ObservableMap<String, Solution> versionToSolution;
	private Solution newest;
	private Solution newestInstalled;
	private Solution newestLaunched;
	private Solution selected;

	public SolutionBundle(String group, String name) {
		this.group = group;
		this.name = name;
		this.versionToSolution = FXCollections.observableHashMap();
		this.newest = null;
		this.newestInstalled = null;
		this.newestLaunched = null;
		this.selected = null;
		versionToSolution.addListener(this::updateNewest);
	}

	private void updateNewest(Observable observable) {
		versionToSolution.forEach((version, solution) -> {
			if(newest == null || Solution.compareVersions(newest.getVersion(), version) < 0) newest = solution;
			if(solution.isInstalled() && (newestInstalled == null ||
					Solution.compareVersions(newestInstalled.getVersion(), version) < 0))
				newestInstalled = solution;
		});
	}

	public ObservableMap<String, Solution> getSolutions() {
		return versionToSolution;
	}

	public String getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	public Solution getFirstChoice() {
		if(selected != null) return selected;
		if(newestLaunched != null) return newestLaunched;
		if(newestInstalled != null) return newestInstalled;
		return newest;
	}

	public void setSelected(String version) {
		if(selected != null && selected.getVersion().equals(version)) return;
		for (Map.Entry<String, Solution> entry : versionToSolution.entrySet()) {
			String solution_version = entry.getKey();
			Solution solution = entry.getValue();
			if (solution_version.equals(version)) {
				selected = solution;
				break;
			}
		}
	}

	public boolean firstChoiceIsMostRecent() {
		if(selected != null) return selected == newest;
		if(newestLaunched != null) return newestLaunched == newest;
		if(newestInstalled != null) return newestInstalled == newest;
		return true;
	}
}
