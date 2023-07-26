/*
 *  Copyright 2023 Tim Schäfer
 *
 *    Licensed under the MIT License (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        https://github.com/dfsp-spirit/jneuroformats/blob/main/LICENSE or at https://opensource.org/licenses/MIT
 *
 *   Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.rcmd.jneuroformats;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A sequential colormap using the colors from the Viridis colormap from matplotlib.
 * The license for the raw colormap values is the "CC0 No rights reserved" license
 * available at https://creativecommons.org/about/cc0, as explained
 * at https://bids.github.io/colormap/.
 *
 * This is just a demo, you will probably want to use a more sophisticated
 * colormap implementation with more maps and support for sequential, diverging,
 * ciclic, and qualitative colormaps, and more colormaps, e.g., from https://github.com/mahdilamb/colormap.
 *
 */
public class Viridis implements Colormap {

    private Color lowColor = null;
    private Color highColor = null;
    private Color nanColor = Color.WHITE;

    public Viridis(Color lowColor, Color highColor, Color nanColor) {
        this.lowColor = lowColor;
        this.highColor = highColor;
        this.nanColor = nanColor;
    }

    private Color getLowColor() {
        if (this.lowColor == null) {
            return this.colors.get(0);
        }
        return this.lowColor;
    }

    private Color getHighColor() {
        if (this.highColor == null) {
            return this.colors.get(colors.size() - 1);
        }
        return this.highColor;
    }

    private Color getNanColor() {
        if (this.nanColor == null) {
            return Color.WHITE;
        }
        return this.nanColor;
    }

    public Viridis() {
    }

    @Override
    public Color get(Float position) {
        if (position == null || !Float.isFinite(position)) {
            return this.getNanColor();
        }
        if (position < 0) {
            return this.getLowColor();
        }
        else if (position > 1) {
            return this.getHighColor();
        }
        else {
            if (colors.size() <= 1) {
                return colors.get(0);
            }
            else {
                float pos = position * (colors.size() - 1);

                int floor = (int) pos;
                if (pos == floor) {
                    return colors.get(floor);
                }
                if (pos == floor + 1) {
                    return colors.get(floor + 1);
                }
                return colors.get(floor); // We could do fancy interpolation here.
            }
        }
    }

    /**
     * The colors from the Viridis colormap from matplotlib.
     * The license for the raw colormap values is the "CC0 No rights reserved" license
     * available at https://creativecommons.org/about/cc0, as explained
     * at https://bids.github.io/colormap/.
     */
    private final List<Color> colors = new ArrayList<>(Arrays.asList(
            new Color(0.267004f, 0.004874f, 0.329415f),
            new Color(0.268510f, 0.009605f, 0.335427f),
            new Color(0.269944f, 0.014625f, 0.341379f),
            new Color(0.271305f, 0.019942f, 0.347269f),
            new Color(0.272594f, 0.025563f, 0.353093f),
            new Color(0.273809f, 0.031497f, 0.358853f),
            new Color(0.274952f, 0.037752f, 0.364543f),
            new Color(0.276022f, 0.044167f, 0.370164f),
            new Color(0.277018f, 0.050344f, 0.375715f),
            new Color(0.277941f, 0.056324f, 0.381191f),
            new Color(0.278791f, 0.062145f, 0.386592f),
            new Color(0.279566f, 0.067836f, 0.391917f),
            new Color(0.280267f, 0.073417f, 0.397163f),
            new Color(0.280894f, 0.078907f, 0.402329f),
            new Color(0.281446f, 0.084320f, 0.407414f),
            new Color(0.281924f, 0.089666f, 0.412415f),
            new Color(0.282327f, 0.094955f, 0.417331f),
            new Color(0.282656f, 0.100196f, 0.422160f),
            new Color(0.282910f, 0.105393f, 0.426902f),
            new Color(0.283091f, 0.110553f, 0.431554f),
            new Color(0.283197f, 0.115680f, 0.436115f),
            new Color(0.283229f, 0.120777f, 0.440584f),
            new Color(0.283187f, 0.125848f, 0.444960f),
            new Color(0.283072f, 0.130895f, 0.449241f),
            new Color(0.282884f, 0.135920f, 0.453427f),
            new Color(0.282623f, 0.140926f, 0.457517f),
            new Color(0.282290f, 0.145912f, 0.461510f),
            new Color(0.281887f, 0.150881f, 0.465405f),
            new Color(0.281412f, 0.155834f, 0.469201f),
            new Color(0.280868f, 0.160771f, 0.472899f),
            new Color(0.280255f, 0.165693f, 0.476498f),
            new Color(0.279574f, 0.170599f, 0.479997f),
            new Color(0.278826f, 0.175490f, 0.483397f),
            new Color(0.278012f, 0.180367f, 0.486697f),
            new Color(0.277134f, 0.185228f, 0.489898f),
            new Color(0.276194f, 0.190074f, 0.493001f),
            new Color(0.275191f, 0.194905f, 0.496005f),
            new Color(0.274128f, 0.199721f, 0.498911f),
            new Color(0.273006f, 0.204520f, 0.501721f),
            new Color(0.271828f, 0.209303f, 0.504434f),
            new Color(0.270595f, 0.214069f, 0.507052f),
            new Color(0.269308f, 0.218818f, 0.509577f),
            new Color(0.267968f, 0.223549f, 0.512008f),
            new Color(0.266580f, 0.228262f, 0.514349f),
            new Color(0.265145f, 0.232956f, 0.516599f),
            new Color(0.263663f, 0.237631f, 0.518762f),
            new Color(0.262138f, 0.242286f, 0.520837f),
            new Color(0.260571f, 0.246922f, 0.522828f),
            new Color(0.258965f, 0.251537f, 0.524736f),
            new Color(0.257322f, 0.256130f, 0.526563f),
            new Color(0.255645f, 0.260703f, 0.528312f),
            new Color(0.253935f, 0.265254f, 0.529983f),
            new Color(0.252194f, 0.269783f, 0.531579f),
            new Color(0.250425f, 0.274290f, 0.533103f),
            new Color(0.248629f, 0.278775f, 0.534556f),
            new Color(0.246811f, 0.283237f, 0.535941f),
            new Color(0.244972f, 0.287675f, 0.537260f),
            new Color(0.243113f, 0.292092f, 0.538516f),
            new Color(0.241237f, 0.296485f, 0.539709f),
            new Color(0.239346f, 0.300855f, 0.540844f),
            new Color(0.237441f, 0.305202f, 0.541921f),
            new Color(0.235526f, 0.309527f, 0.542944f),
            new Color(0.233603f, 0.313828f, 0.543914f),
            new Color(0.231674f, 0.318106f, 0.544834f),
            new Color(0.229739f, 0.322361f, 0.545706f),
            new Color(0.227802f, 0.326594f, 0.546532f),
            new Color(0.225863f, 0.330805f, 0.547314f),
            new Color(0.223925f, 0.334994f, 0.548053f),
            new Color(0.221989f, 0.339161f, 0.548752f),
            new Color(0.220057f, 0.343307f, 0.549413f),
            new Color(0.218130f, 0.347432f, 0.550038f),
            new Color(0.216210f, 0.351535f, 0.550627f),
            new Color(0.214298f, 0.355619f, 0.551184f),
            new Color(0.212395f, 0.359683f, 0.551710f),
            new Color(0.210503f, 0.363727f, 0.552206f),
            new Color(0.208623f, 0.367752f, 0.552675f),
            new Color(0.206756f, 0.371758f, 0.553117f),
            new Color(0.204903f, 0.375746f, 0.553533f),
            new Color(0.203063f, 0.379716f, 0.553925f),
            new Color(0.201239f, 0.383670f, 0.554294f),
            new Color(0.199430f, 0.387607f, 0.554642f),
            new Color(0.197636f, 0.391528f, 0.554969f),
            new Color(0.195860f, 0.395433f, 0.555276f),
            new Color(0.194100f, 0.399323f, 0.555565f),
            new Color(0.192357f, 0.403199f, 0.555836f),
            new Color(0.190631f, 0.407061f, 0.556089f),
            new Color(0.188923f, 0.410910f, 0.556326f),
            new Color(0.187231f, 0.414746f, 0.556547f),
            new Color(0.185556f, 0.418570f, 0.556753f),
            new Color(0.183898f, 0.422383f, 0.556944f),
            new Color(0.182256f, 0.426184f, 0.557120f),
            new Color(0.180629f, 0.429975f, 0.557282f),
            new Color(0.179019f, 0.433756f, 0.557430f),
            new Color(0.177423f, 0.437527f, 0.557565f),
            new Color(0.175841f, 0.441290f, 0.557685f),
            new Color(0.174274f, 0.445044f, 0.557792f),
            new Color(0.172719f, 0.448791f, 0.557885f),
            new Color(0.171176f, 0.452530f, 0.557965f),
            new Color(0.169646f, 0.456262f, 0.558030f),
            new Color(0.168126f, 0.459988f, 0.558082f),
            new Color(0.166617f, 0.463708f, 0.558119f),
            new Color(0.165117f, 0.467423f, 0.558141f),
            new Color(0.163625f, 0.471133f, 0.558148f),
            new Color(0.162142f, 0.474838f, 0.558140f),
            new Color(0.160665f, 0.478540f, 0.558115f),
            new Color(0.159194f, 0.482237f, 0.558073f),
            new Color(0.157729f, 0.485932f, 0.558013f),
            new Color(0.156270f, 0.489624f, 0.557936f),
            new Color(0.154815f, 0.493313f, 0.557840f),
            new Color(0.153364f, 0.497000f, 0.557724f),
            new Color(0.151918f, 0.500685f, 0.557587f),
            new Color(0.150476f, 0.504369f, 0.557430f),
            new Color(0.149039f, 0.508051f, 0.557250f),
            new Color(0.147607f, 0.511733f, 0.557049f),
            new Color(0.146180f, 0.515413f, 0.556823f),
            new Color(0.144759f, 0.519093f, 0.556572f),
            new Color(0.143343f, 0.522773f, 0.556295f),
            new Color(0.141935f, 0.526453f, 0.555991f),
            new Color(0.140536f, 0.530132f, 0.555659f),
            new Color(0.139147f, 0.533812f, 0.555298f),
            new Color(0.137770f, 0.537492f, 0.554906f),
            new Color(0.136408f, 0.541173f, 0.554483f),
            new Color(0.135066f, 0.544853f, 0.554029f),
            new Color(0.133743f, 0.548535f, 0.553541f),
            new Color(0.132444f, 0.552216f, 0.553018f),
            new Color(0.131172f, 0.555899f, 0.552459f),
            new Color(0.129933f, 0.559582f, 0.551864f),
            new Color(0.128729f, 0.563265f, 0.551229f),
            new Color(0.127568f, 0.566949f, 0.550556f),
            new Color(0.126453f, 0.570633f, 0.549841f),
            new Color(0.125394f, 0.574318f, 0.549086f),
            new Color(0.124395f, 0.578002f, 0.548287f),
            new Color(0.123463f, 0.581687f, 0.547445f),
            new Color(0.122606f, 0.585371f, 0.546557f),
            new Color(0.121831f, 0.589055f, 0.545623f),
            new Color(0.121148f, 0.592739f, 0.544641f),
            new Color(0.120565f, 0.596422f, 0.543611f),
            new Color(0.120092f, 0.600104f, 0.542530f),
            new Color(0.119738f, 0.603785f, 0.541400f),
            new Color(0.119512f, 0.607464f, 0.540218f),
            new Color(0.119423f, 0.611141f, 0.538982f),
            new Color(0.119483f, 0.614817f, 0.537692f),
            new Color(0.119699f, 0.618490f, 0.536347f),
            new Color(0.120081f, 0.622161f, 0.534946f),
            new Color(0.120638f, 0.625828f, 0.533488f),
            new Color(0.121380f, 0.629492f, 0.531973f),
            new Color(0.122312f, 0.633153f, 0.530398f),
            new Color(0.123444f, 0.636809f, 0.528763f),
            new Color(0.124780f, 0.640461f, 0.527068f),
            new Color(0.126326f, 0.644107f, 0.525311f),
            new Color(0.128087f, 0.647749f, 0.523491f),
            new Color(0.130067f, 0.651384f, 0.521608f),
            new Color(0.132268f, 0.655014f, 0.519661f),
            new Color(0.134692f, 0.658636f, 0.517649f),
            new Color(0.137339f, 0.662252f, 0.515571f),
            new Color(0.140210f, 0.665859f, 0.513427f),
            new Color(0.143303f, 0.669459f, 0.511215f),
            new Color(0.146616f, 0.673050f, 0.508936f),
            new Color(0.150148f, 0.676631f, 0.506589f),
            new Color(0.153894f, 0.680203f, 0.504172f),
            new Color(0.157851f, 0.683765f, 0.501686f),
            new Color(0.162016f, 0.687316f, 0.499129f),
            new Color(0.166383f, 0.690856f, 0.496502f),
            new Color(0.170948f, 0.694384f, 0.493803f),
            new Color(0.175707f, 0.697900f, 0.491033f),
            new Color(0.180653f, 0.701402f, 0.488189f),
            new Color(0.185783f, 0.704891f, 0.485273f),
            new Color(0.191090f, 0.708366f, 0.482284f),
            new Color(0.196571f, 0.711827f, 0.479221f),
            new Color(0.202219f, 0.715272f, 0.476084f),
            new Color(0.208030f, 0.718701f, 0.472873f),
            new Color(0.214000f, 0.722114f, 0.469588f),
            new Color(0.220124f, 0.725509f, 0.466226f),
            new Color(0.226397f, 0.728888f, 0.462789f),
            new Color(0.232815f, 0.732247f, 0.459277f),
            new Color(0.239374f, 0.735588f, 0.455688f),
            new Color(0.246070f, 0.738910f, 0.452024f),
            new Color(0.252899f, 0.742211f, 0.448284f),
            new Color(0.259857f, 0.745492f, 0.444467f),
            new Color(0.266941f, 0.748751f, 0.440573f),
            new Color(0.274149f, 0.751988f, 0.436601f),
            new Color(0.281477f, 0.755203f, 0.432552f),
            new Color(0.288921f, 0.758394f, 0.428426f),
            new Color(0.296479f, 0.761561f, 0.424223f),
            new Color(0.304148f, 0.764704f, 0.419943f),
            new Color(0.311925f, 0.767822f, 0.415586f),
            new Color(0.319809f, 0.770914f, 0.411152f),
            new Color(0.327796f, 0.773980f, 0.406640f),
            new Color(0.335885f, 0.777018f, 0.402049f),
            new Color(0.344074f, 0.780029f, 0.397381f),
            new Color(0.352360f, 0.783011f, 0.392636f),
            new Color(0.360741f, 0.785964f, 0.387814f),
            new Color(0.369214f, 0.788888f, 0.382914f),
            new Color(0.377779f, 0.791781f, 0.377939f),
            new Color(0.386433f, 0.794644f, 0.372886f),
            new Color(0.395174f, 0.797475f, 0.367757f),
            new Color(0.404001f, 0.800275f, 0.362552f),
            new Color(0.412913f, 0.803041f, 0.357269f),
            new Color(0.421908f, 0.805774f, 0.351910f),
            new Color(0.430983f, 0.808473f, 0.346476f),
            new Color(0.440137f, 0.811138f, 0.340967f),
            new Color(0.449368f, 0.813768f, 0.335384f),
            new Color(0.458674f, 0.816363f, 0.329727f),
            new Color(0.468053f, 0.818921f, 0.323998f),
            new Color(0.477504f, 0.821444f, 0.318195f),
            new Color(0.487026f, 0.823929f, 0.312321f),
            new Color(0.496615f, 0.826376f, 0.306377f),
            new Color(0.506271f, 0.828786f, 0.300362f),
            new Color(0.515992f, 0.831158f, 0.294279f),
            new Color(0.525776f, 0.833491f, 0.288127f),
            new Color(0.535621f, 0.835785f, 0.281908f),
            new Color(0.545524f, 0.838039f, 0.275626f),
            new Color(0.555484f, 0.840254f, 0.269281f),
            new Color(0.565498f, 0.842430f, 0.262877f),
            new Color(0.575563f, 0.844566f, 0.256415f),
            new Color(0.585678f, 0.846661f, 0.249897f),
            new Color(0.595839f, 0.848717f, 0.243329f),
            new Color(0.606045f, 0.850733f, 0.236712f),
            new Color(0.616293f, 0.852709f, 0.230052f),
            new Color(0.626579f, 0.854645f, 0.223353f),
            new Color(0.636902f, 0.856542f, 0.216620f),
            new Color(0.647257f, 0.858400f, 0.209861f),
            new Color(0.657642f, 0.860219f, 0.203082f),
            new Color(0.668054f, 0.861999f, 0.196293f),
            new Color(0.678489f, 0.863742f, 0.189503f),
            new Color(0.688944f, 0.865448f, 0.182725f),
            new Color(0.699415f, 0.867117f, 0.175971f),
            new Color(0.709898f, 0.868751f, 0.169257f),
            new Color(0.720391f, 0.870350f, 0.162603f),
            new Color(0.730889f, 0.871916f, 0.156029f),
            new Color(0.741388f, 0.873449f, 0.149561f),
            new Color(0.751884f, 0.874951f, 0.143228f),
            new Color(0.762373f, 0.876424f, 0.137064f),
            new Color(0.772852f, 0.877868f, 0.131109f),
            new Color(0.783315f, 0.879285f, 0.125405f),
            new Color(0.793760f, 0.880678f, 0.120005f),
            new Color(0.804182f, 0.882046f, 0.114965f),
            new Color(0.814576f, 0.883393f, 0.110347f),
            new Color(0.824940f, 0.884720f, 0.106217f),
            new Color(0.835270f, 0.886029f, 0.102646f),
            new Color(0.845561f, 0.887322f, 0.099702f),
            new Color(0.855810f, 0.888601f, 0.097452f),
            new Color(0.866013f, 0.889868f, 0.095953f),
            new Color(0.876168f, 0.891125f, 0.095250f),
            new Color(0.886271f, 0.892374f, 0.095374f),
            new Color(0.896320f, 0.893616f, 0.096335f),
            new Color(0.906311f, 0.894855f, 0.098125f),
            new Color(0.916242f, 0.896091f, 0.100717f),
            new Color(0.926106f, 0.897330f, 0.104071f),
            new Color(0.935904f, 0.898570f, 0.108131f),
            new Color(0.945636f, 0.899815f, 0.112838f),
            new Color(0.955300f, 0.901065f, 0.118128f),
            new Color(0.964894f, 0.902323f, 0.123941f),
            new Color(0.974417f, 0.903590f, 0.130215f),
            new Color(0.983868f, 0.904867f, 0.136897f),
            new Color(0.993248f, 0.906157f, 0.143936f)

    ));

}