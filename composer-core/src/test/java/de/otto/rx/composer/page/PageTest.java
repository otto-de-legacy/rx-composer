package de.otto.rx.composer.page;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.*;
import de.otto.rx.composer.context.RequestContext;
import org.junit.Test;

import java.time.LocalDateTime;

import static com.google.common.collect.ImmutableMap.of;
import static de.otto.rx.composer.content.AbcPosition.X;
import static de.otto.rx.composer.content.AbcPosition.Y;
import static de.otto.rx.composer.content.Headers.emptyHeaders;
import static de.otto.rx.composer.content.Parameters.emptyParameters;
import static de.otto.rx.composer.content.Parameters.parameters;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.Observable.empty;
import static rx.Observable.just;

public class PageTest {

    @Test
    public void shouldCreatePlanWithSomeFragments() {
        // given
        final Fragment doFirst = mock(Fragment.class);
        final Fragment doSecond = mock(Fragment.class);
        when(doFirst.getPosition()).thenReturn(X);
        when(doSecond.getPosition()).thenReturn(Y);
        final Page page = Page.consistsOf(
                doFirst, doSecond
        );
        // when
        final ImmutableList<Fragment> fragments = page.getFragments();
        // then
        assertThat(fragments, hasSize(2));
        assertThat(fragments.get(0).getPosition(), is(X));
        assertThat(fragments.get(1).getPosition(), is(Y));
    }

    @Test
    public void shouldExecuteFragments() {
        // given
        final Fragment doFirst = mock(Fragment.class);
        final Fragment doSecond = mock(Fragment.class);
        final Page page = Page.consistsOf(
                        doFirst, doSecond
        );
        // when
        page.fetchWith(emptyParameters());
        // then
        verify(doFirst).fetchWith(any(RequestContext.class), any(Parameters.class));
        verify(doSecond).fetchWith(any(RequestContext.class), any(Parameters.class));
    }

    @Test
    public void shouldReturnContents() {
        // given
        final Fragment doFirst = mock(Fragment.class);
        when(doFirst.getPosition()).thenReturn(X);
        when(doFirst.fetchWith(any(RequestContext.class), any(Parameters.class))).thenReturn(just(someContent("test", X, "Foo")));
        // and
        final Page page = Page.consistsOf(
                doFirst
        );
        // when
        final Contents contents = page.fetchWith(emptyParameters());
        // then
        assertThat(contents.getBody(X), is("Foo"));
    }

    @Test
    public void shouldIgnoreFragmentsWithEmptyResults() {
        // given
        final Fragment doFirst = mock(Fragment.class);
        when(doFirst.getPosition()).thenReturn(X);
        when(doFirst.fetchWith(any(RequestContext.class), any(Parameters.class))).thenReturn(empty());
        // and
        final Page page = Page.consistsOf(
                doFirst
        );
        // when
        final Contents contents = page.fetchWith(emptyParameters());
        // then
        assertThat(contents.get(X).isAvailable(), is(false));
    }

    @Test
    public void shouldExecutePlanMultipleTimes() {
        // given
        final Fragment doFirst = mock(Fragment.class);
        final Fragment doSecond = mock(Fragment.class);
        final Page page = Page.consistsOf(
                        doFirst, doSecond
        );
        // when
        page.fetchWith(emptyParameters());
        page.fetchWith(emptyParameters());
        // then
        verify(doFirst, times(2)).fetchWith(any(RequestContext.class), any(Parameters.class));
        verify(doSecond, times(2)).fetchWith(any(RequestContext.class), any(Parameters.class));
    }

    @Test
    public void shouldForwardParameters() {
        // given
        final Parameters someParameters = parameters(of(
                "message", "World"
        ));
        final Fragment doFirst = mock(Fragment.class);
        final Fragment doSecond = mock(Fragment.class);
        final Page page = Page.consistsOf(
                doFirst, doSecond
        );
        // when
        page.fetchWith(someParameters);
        // then
        verify(doFirst).fetchWith(any(RequestContext.class), eq(someParameters));
        verify(doSecond).fetchWith(any(RequestContext.class), eq(someParameters));
    }

    private Content someContent(final String source, final Position position, final String text) {
        return new TestContent(source, position, text);
    }

    private static final class TestContent extends SingleContent {
        private final String source;
        private final String text;
        private final Position position;

        TestContent(final String source,
                    final Position position,
                    final String text) {
            this.source = source;
            this.position = position;
            this.text = text;
        }

        @Override
        public String getSource() {
            return source;
        }

        @Override
        public Position getPosition() {
            return position;
        }

        @Override
        public boolean isAvailable() {
            return !text.isEmpty();
        }

        @Override
        public String getBody() {
            return text;
        }

        @Override
        public Headers getHeaders() {
            return emptyHeaders();
        }

        @Override
        public LocalDateTime getCreated() {
            return now();
        }

    }
}