package com.digambergupta.kubernetescontroller.model;

import java.util.Objects;

public class DeploymentInfo {
	private final String kind;

	private final String metadataName;

	public DeploymentInfo(String kind, String metadataName) {
		this.kind = kind;
		this.metadataName = metadataName;
	}

	public String getKind() {
		return kind;
	}

	public String getMetadataName() {
		return metadataName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DeploymentInfo that = (DeploymentInfo) o;
		return Objects.equals(kind, that.kind) && Objects.equals(metadataName, that.metadataName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, metadataName);
	}
}
