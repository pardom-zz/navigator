package navigator

import android.support.test.InstrumentationRegistry
import android.support.test.annotation.UiThreadTest
import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.ViewGroup
import navigator.mock.MockNavigator
import navigator.mock.MockRoute
import navigator.util.TestActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class NavigatorTest {

    @get:Rule
    val activityRule = ActivityTestRule(TestActivity::class.java)
    @get:Rule
    val expectedException = ExpectedException.none()

    private lateinit var parent: ViewGroup
    private lateinit var navigator: MockNavigator

    @Before
    fun setUp() {
        parent = activityRule.activity.parent
        navigator = MockNavigator(InstrumentationRegistry.getTargetContext())
    }

    @Test
    @UiThreadTest
    fun testPush() {
        val routeA = MockRoute()
        val routeB = MockRoute()
        val routeC = MockRoute()
        navigator.push(routeA)
        navigator.push(routeB)
        navigator.push(routeC)
        assertThat(navigator.routes).containsExactlyElementsOf(listOf(
                routeA, routeB, routeC
        ))
    }

    @Test
    @UiThreadTest
    fun testPushNamed() {
        val routesSize = navigator.routes.size
        navigator.pushNamed("mock")
        assertThat(navigator.routes.size).isEqualTo(routesSize + 1)
    }

    @Test
    @UiThreadTest
    fun testReplaceFirst() {
        val routeA = MockRoute()
        val routeB = MockRoute()
        val routeC = MockRoute()
        val routeD = MockRoute()
        navigator.push(routeA)
        navigator.push(routeB)
        navigator.push(routeC)
        navigator.replace(routeA, routeD)
        assertThat(navigator.routes).containsExactlyElementsOf(listOf(
                routeD, routeB, routeC
        ))
    }

    @Test
    @UiThreadTest
    fun testReplaceMiddle() {
        val routeA = MockRoute()
        val routeB = MockRoute()
        val routeC = MockRoute()
        val routeD = MockRoute()
        navigator.push(routeA)
        navigator.push(routeB)
        navigator.push(routeC)
        navigator.replace(routeB, routeD)
        assertThat(navigator.routes).containsExactlyElementsOf(listOf(
                routeA, routeD, routeC
        ))
    }

    @Test
    @UiThreadTest
    fun testReplaceLast() {
        val routeA = MockRoute()
        val routeB = MockRoute()
        val routeC = MockRoute()
        val routeD = MockRoute()
        navigator.push(routeA)
        navigator.push(routeB)
        navigator.push(routeC)
        navigator.replace(routeC, routeD)
        assertThat(navigator.routes).containsExactlyElementsOf(listOf(
                routeA, routeB, routeD
        ))
    }

    @Test
    @UiThreadTest
    fun testPushReplacement() {
        val routeA = MockRoute()
        val routeB = MockRoute()
        val routeC = MockRoute()
        navigator.push(routeA)
        navigator.push(routeB)
        navigator.pushReplacement(routeC)
        assertThat(navigator.routes).containsExactlyElementsOf(listOf(
                routeA, routeC
        ))
    }

    @Test
    @UiThreadTest
    fun testPushReplacementResult() {
        val routeA = MockRoute()
        val routeB = MockRoute()
        navigator.push(routeA)
        val result = navigator.pushReplacement(routeB, "result")
        assertThat(result.getCompleted()).isEqualTo("result")
    }

    @Test
    @UiThreadTest
    fun testReplaceRouteBelowFirst() {
        val routeA = MockRoute()
        val routeB = MockRoute()
        val routeC = MockRoute()
        val routeD = MockRoute()
        navigator.push(routeA)
        navigator.push(routeB)
        navigator.push(routeC)
        expectedException.expect(IllegalArgumentException::class.java)
        navigator.replaceRouteBelow(routeA, routeD)
    }

    @Test
    @UiThreadTest
    fun testReplaceRouteBelowMiddle() {
        val routeA = MockRoute()
        val routeB = MockRoute()
        val routeC = MockRoute()
        val routeD = MockRoute()
        navigator.push(routeA)
        navigator.push(routeB)
        navigator.push(routeC)
        navigator.replaceRouteBelow(routeB, routeD)
        assertThat(navigator.routes).containsExactlyElementsOf(listOf(
                routeD, routeB, routeC
        ))
    }

    @Test
    @UiThreadTest
    fun testReplaceRouteBelowLast() {
        val routeA = MockRoute()
        val routeB = MockRoute()
        val routeC = MockRoute()
        val routeD = MockRoute()
        navigator.push(routeA)
        navigator.push(routeB)
        navigator.push(routeC)
        navigator.replaceRouteBelow(routeC, routeD)
        assertThat(navigator.routes).containsExactlyElementsOf(listOf(
                routeA, routeD, routeC
        ))
    }

    @Test
    @UiThreadTest
    fun testRemoveRoute() {
        val routeA = MockRoute()
        val routeB = MockRoute()
        val routeC = MockRoute()
        navigator.push(routeA)
        navigator.push(routeB)
        navigator.push(routeC)
        navigator.removeRoute(routeB)
        assertThat(navigator.routes).containsExactlyElementsOf(listOf(
                routeA, routeC
        ))
    }

    @Test
    @UiThreadTest
    fun testCanPopEmpty() {
        expectedException.expect(IllegalArgumentException::class.java)
        navigator.canPop()
    }

    @Test
    @UiThreadTest
    fun testCanPopSingle() {
        navigator.push(MockRoute())
        val canPop = navigator.canPop()
        assertThat(canPop).isFalse()
    }

    @Test
    @UiThreadTest
    fun testCanPopTrue() {
        navigator.push(MockRoute())
        navigator.push(MockRoute())
        val canPop = navigator.canPop()
        assertThat(canPop).isTrue()
    }

}
