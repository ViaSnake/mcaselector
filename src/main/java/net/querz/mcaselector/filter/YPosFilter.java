package net.querz.mcaselector.filter;

import net.querz.mcaselector.io.mca.ChunkData;
import net.querz.mcaselector.version.ChunkFilter;
import net.querz.mcaselector.version.VersionController;
import net.querz.nbt.IntTag;

public class YPosFilter extends IntFilter {

	public YPosFilter() {
		this(Operator.AND, Comparator.EQUAL, 0);
	}

	public YPosFilter(Operator operator, Comparator comparator, int value) {
		super(FilterType.Y_POS, operator, comparator, value);
	}

	@Override
	protected Integer getNumber(ChunkData data) {
		if (data.region() == null || data.region().getData() == null) {
			return null;
		}
		ChunkFilter chunkFilter = VersionController.getChunkFilter(data.region().getData().getInt("DataVersion"));
		IntTag tag = chunkFilter.getYPos(data.region().getData());
		return tag == null ? 0 : tag.asInt();
	}

	@Override
	public YPosFilter clone() {
		return new YPosFilter(getOperator(), getComparator(), value);
	}
}
