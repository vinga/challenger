package com.kameo.challenger.utils.odb;

import javax.persistence.TypedQuery;
import java.util.Optional;

public interface IQueryPage {
	class Rows {
		private Integer firstRow;
		private Integer maxRows;

		public static Rows none() {
			return new Rows(null, null);
		}

		public final Rows newRowsPlusOne() {
			return new Rows(firstRow, maxRows + 1);
		}

		public Rows(Integer firstRow, Integer maxRows) {
			this.firstRow = firstRow;
			this.maxRows = maxRows;
		}

		public Rows withFirstRow(Integer firstRow) {
			this.firstRow = firstRow;
			return this;
		}

		public Rows withMaxRows(Integer maxRows) {
			this.maxRows = maxRows;
			return this;
		}

		public void applyToQuery(TypedQuery<?> query) {
			if (firstRow != null)
				query.setFirstResult(firstRow);
			if (maxRows != null)
				query.setMaxResults(maxRows);
		}

		public static Rows max(int maxRows) {
			return new Rows(null, null).withMaxRows(maxRows);
		}

		public Optional<Integer> getMaxRows() {
			return Optional.ofNullable(maxRows);
		}

		public void prevPage() {
			firstRow = Math.max(0, firstRow - maxRows);
		}

		public void nextPage() {
			firstRow += maxRows;
		}

		public Integer getFirstRow() {
			return firstRow;
		}
	}

	Rows getRows();

}
