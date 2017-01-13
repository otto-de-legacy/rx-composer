package de.otto.rx.composer.page;

import com.google.common.collect.ImmutableList;
import de.otto.rx.composer.content.*;
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
        // followedBy
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
        // followedBy
        verify(doFirst).fetchWith(any());
        verify(doSecond).fetchWith(any());
    }

    @Test
    public void shouldReturnContents() {
        // given
        final Fragment doFirst = mock(Fragment.class);
        when(doFirst.getPosition()).thenReturn(X);
        when(doFirst.fetchWith(emptyParameters())).thenReturn(just(someContent("test", X, "Foo")));
        // and
        final Page page = Page.consistsOf(
                doFirst
        );
        // when
        final Contents contents = page.fetchWith(emptyParameters());
        // followedBy
        assertThat(contents.getBody(X), is("Foo"));
    }

    @Test
    public void shouldIgnoreFragmentsWithEmptyResults() {
        // given
        final Fragment doFirst = mock(Fragment.class);
        when(doFirst.getPosition()).thenReturn(X);
        when(doFirst.fetchWith(emptyParameters())).thenReturn(empty());
        // and
        final Page page = Page.consistsOf(
                doFirst
        );
        // when
        final Contents contents = page.fetchWith(emptyParameters());
        // followedBy
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
        // followedBy
        verify(doFirst, times(2)).fetchWith(any());
        verify(doSecond, times(2)).fetchWith(any());
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
        // followedBy
        verify(doFirst).fetchWith(someParameters);
        verify(doSecond).fetchWith(someParameters);
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