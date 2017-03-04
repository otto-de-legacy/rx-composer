package de.otto.rx.composer.thymeleaf;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.Set;

import static java.util.Collections.singleton;

public class RxComposerDialect extends AbstractProcessorDialect {

    public RxComposerDialect() {
        super("rxComposerDialect", "rxc", 10000);
    }

    @Override
    public Set<IProcessor> getProcessors(final String dialectPrefix) {
        return singleton(new RxcContentElementProcessor());
    }
}
