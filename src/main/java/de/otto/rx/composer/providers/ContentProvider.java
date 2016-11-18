package de.otto.rx.composer.providers;

import de.otto.rx.composer.content.Content;
import de.otto.rx.composer.content.Parameters;
import de.otto.rx.composer.content.Position;
import rx.Observable;

public interface ContentProvider {

    Observable<Content> getContent(final Position position, final Parameters parameters);
}
