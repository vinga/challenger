package challenger.utils.odb;

import com.google.common.base.Optional;

import javax.persistence.TypedQuery;

public interface IQueryPage {
	public static class Rows {
		private Integer firstRow;
		private Integer maxRows;

		public static final Rows none() {
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
			return Optional.fromNullable(maxRows);
		}

		public void prevPage() {
			firstRow = Math.max(0, firstRow - maxRows);
		}

		public void nextPage() {
			firstRow = firstRow += maxRows;
		}

		public Integer getFirstRow() {
			return firstRow;
		}
	}

	public Rows getRows();

}
