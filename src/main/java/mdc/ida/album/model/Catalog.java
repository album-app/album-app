package mdc.ida.album.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Catalog {

	private final AlbumInstallation parent;
	private final StringProperty name = new SimpleStringProperty();
	private final StringProperty path = new SimpleStringProperty();
	private final StringProperty src = new SimpleStringProperty();
	private final IntegerProperty id = new SimpleIntegerProperty();
	private final BooleanProperty isLocal = new SimpleBooleanProperty();
	private final ArrayList<Solution> solutions;
	private final Map<String, SolutionBundle> solutionBundles;

	public Catalog(AlbumInstallation parent, ArrayList<Solution> solutions) {
		this.parent = parent;
		this.solutions = solutions;
		this.solutionBundles = toBundles(solutions);
	}

	private Map<String, SolutionBundle> toBundles(ArrayList<Solution> solutions) {
		Map<String, SolutionBundle> res = new HashMap<>();
		for (Solution solution : solutions) {
			String key = asBundleKey(solution);
			if(!res.containsKey(key)) {
				res.put(key, new SolutionBundle(solution.getGroup(), solution.getName()));
			}
			res.get(key).getSolutions().put(solution.getVersion(), solution);
		}
		return res;
	}

	private String asBundleKey(Solution solution) {
		return solution.getGroup() + ":" + solution.getName();
	}

	public ArrayList<Solution> getSolutions() {
		return solutions;
	}

	public Map<String, SolutionBundle> getSolutionBundles() {
		return solutionBundles;
	}

	public boolean isIsLocal() {
		return isLocal.get();
	}

	public BooleanProperty isLocalProperty() {
		return isLocal;
	}

	public void setIsLocal(boolean isLocal) {
		this.isLocal.set(isLocal);
	}

	public String getName() {
		return name.get();
	}

	public StringProperty nameProperty() {
		return name;
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public AlbumInstallation getParent() {
		return parent;
	}

	public void setSrc(String src) {
		this.src.set(src);
	}

	public void setPath(String path) {
		this.path.set(path);
	}

	public String getPath() {
		return path.get();
	}

	public int getId() {
		return id.get();
	}

	public void setId(int id) {
		this.id.set(id);
	}

}
