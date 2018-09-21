package com.applause.auto.pageframework.utils;

import com.applause.auto.framework.pageframework.web.AbstractPageChunk;
import com.applause.auto.framework.pageframework.web.ChunkFactory;

public final class BDChunkFactory {
	public static <T extends AbstractPageChunk> T create(final Class<T> clazz, final Object... args) {
		try (AutoTimer ignored = AutoTimer.start("ChunkFactory.create")) {
			return ChunkFactory.create(clazz, args);
		}
	}
}
