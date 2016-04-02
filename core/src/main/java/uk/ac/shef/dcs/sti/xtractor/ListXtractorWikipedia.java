package uk.ac.shef.dcs.sti.xtractor;

import info.bliki.wiki.model.WikiModel;
import org.w3c.dom.Node;
import uk.ac.shef.dcs.sti.core.model.LList;

import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 09/10/12
 * Time: 11:19
 * <p/>
 * selects
 */
public class ListXtractorWikipedia extends ListXtractorHTML {
    private WikiModel model;

    private final String[] STOP_HEADINGS = {"see also",
            "external links", "further readings", "other readings"}; //a list of headings that if found for a list, the list should be discarded

    public ListXtractorWikipedia(ListElementTokenizer tokenizer, ListValidator... validators) {
        super(tokenizer, validators);
        model = new WikiModel("/${image}", "/${title}");
    }

    @Override
    public List<LList> extract(String input, String sourceId) {
        String html = model.render(input);
        return super.extract(html, sourceId);
    }

    @Override
    protected boolean isValidPosition(Node ulElement) {
        Node par = ulElement.getParentNode();
        if (par != null && !par.getNodeName().equalsIgnoreCase("body"))
            return false;

        Node previousSibling = ulElement.getPreviousSibling();
        while (previousSibling != null) {
            if (previousSibling.getNodeName().toLowerCase().startsWith("h")) {
                String headerText = previousSibling.getTextContent();
                for(String stopHeading: STOP_HEADINGS){
                    if(headerText.equalsIgnoreCase(stopHeading))
                        return false;
                }
                return true;
            } else
                previousSibling = previousSibling.getPreviousSibling();
        }

        return true;
    }

    /*public static void main(String[] args) {
        ListXtractor xtractor = new ListXtractorWikipedia(new ListElementTokenizerByURL());

        String wikitext="{{Politics of Nepal}}\n" +
                "[[Image:Nepal districts.png|left|350px|thumb|Districts of Nepal]]\n" +
                "\n" +
                "[[Nepal]]'s 14 [[Zones of Nepal|administrative zones]] (Nepali: अञ्चल; anchal) are subdivided into 75 districts (Nepali:जिल्ला; jillā). These districts are listed below, by zone.  District headquarters are in parentheses.\n" +
                "{{TOC left}}\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Bagmati Zone]]==\n" +
                "[[Image:Bagmati districts.png|thumb|right|250px|Districts of Bagmati]]\n" +
                "*[[Bhaktapur District]] ([[Bhaktapur]])\n" +
                "*[[Dhading District]] ([[Dhading Besi]])\n" +
                "*[[Kathmandu District]] ([[Kathmandu]])\n" +
                "*[[Kavrepalanchok District]] ([[Dhulikhel]])\n" +
                "*[[Lalitpur District, Nepal|Lalitpur District]] ([[Patan, Lalitpur|Patan]])\n" +
                "*[[Nuwakot District]] ([[Bidur]])\n" +
                "*[[Rasuwa District]] ([[Dhunche]])\n" +
                "*[[Sindhupalchok District]] ([[Chautara]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Bheri Zone]]==\n" +
                "[[Image:Bheri districts.png|thumb|right|250px|Districts of Bheri]]\n" +
                "*[[Banke District]] ([[Nepalgunj]])\n" +
                "*[[Bardiya District]] ([[Gulariya Nepal|Gulariya]])\n" +
                "*[[Dailekh District]]  ([[Dullu]])\n" +
                "*[[Jajarkot District]] ([[Khalanga, Jajarkot|Khalanga]])\n" +
                "*[[Surkhet District]] ([[Birendranagar]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Dhawalagiri Zone]]==\n" +
                "[[Image:Dhawalagiri districts.png|thumb|right|250px|Districts of Dhawalagiri]]\n" +
                "*[[Baglung District]] ([[Baglung]])\n" +
                "*[[Mustang District]] ([[Jomsom]])\n" +
                "*[[Myagdi District]] ([[Beni, Dhawalagiri|Beni]])\n" +
                "*[[Parbat District]] ([[Kusma, Nepal|Kusma]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Gandaki Zone]]==\n" +
                "[[Image:Gandaki districts.png|thumb|right|250px|Districts of Gandaki]]\n" +
                "*[[Gorkha District]] ([[Prithbinarayan|Gorkha]])\n" +
                "*[[Kaski District]] ([[Pokhara]])\n" +
                "*[[Lamjung District]] ([[Besisahar]])\n" +
                "*[[Manang District]] ([[Chame, Nepal|Chame]])\n" +
                "*[[Syangja District]] ([[Syangja]])\n" +
                "*[[Tanahu District]] ([[Damauli]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Janakpur Zone]]==\n" +
                "[[Image:Janakpur districts.png|thumb|right|250px|Districts of Janakpur]]\n" +
                "*[[Dhanusa District]] ([[Janakpur]])\n" +
                "*[[Dolakha District]] ([[Charikot]])\n" +
                "*[[Mahottari District]] ([[Jaleswor]])\n" +
                "*[[Ramechhap District]] ([[Manthali, Janakpur|Manthali]])\n" +
                "*[[Sarlahi District]] ([[Malangwa]])\n" +
                "*[[Sindhuli District]] ([[Kamalamai]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Karnali Zone]]==\n" +
                "[[Image:Karnali districts.png|thumb|right|250px|Districts of Karnali]]\n" +
                "*[[Dolpa District]] ([[Dunai, Nepal|Dunai]])\n" +
                "*[[Humla District]] ([[Simikot]])\n" +
                "*[[Jumla District]] ([[Jumla (town)|Jumla Khalanga]])\n" +
                "*[[Kalikot District]] ([[Manma]])\n" +
                "*[[Mugu District]] ([[Gamgadhi]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Koshi Zone]]==\n" +
                "[[Image:Koshi districts.png|thumb|right|250px|Districts of Koshi]]\n" +
                "*[[Bhojpur District, Nepal|Bhojpur District]] ([[Bhojpur, Nepal|Bhojpur]])\n" +
                "*[[Dhankuta District]] ([[Dhankuta]])\n" +
                "*[[Morang District]] ([[Biratnagar]])\n" +
                "*[[Sankhuwasabha District]] ([[Khandbari]])\n" +
                "*[[Sunsari District]] ([[Inaruwa, Kosi|Inaruwa]])\n" +
                "*[[Terhathum District]] ([[Myanglung]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Lumbini Zone]]==\n" +
                "[[Image:Lumbini districts.png|thumb|right|250px|Districts of Lumbini]]\n" +
                "*[[Arghakhanchi District]] ([[Sandhikharka]])\n" +
                "*[[Gulmi District]] ([[Tamghas]])\n" +
                "*[[Kapilvastu District]] ([[Taulihawa|Kapilvastu]])\n" +
                "*[[Nawalparasi District]] ([[Parasi, Nepal|Parasi]])\n" +
                "*[[Palpa District]] ([[Tansen, Nepal|Tansen]])\n" +
                "*[[Rupandehi District]] ([[Siddharthanagar]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Mahakali Zone]]==\n" +
                "[[Image:Mahakali districts.png|thumb|right|250px|Districts of Mahakali]]\n" +
                "*[[Baitadi District]] ([[Baitadi]])\n" +
                "*[[Dadeldhura District]] ([[Dadeldhura]])\n" +
                "*[[Darchula District]]  ([[Darchula]])\n" +
                "*[[Kanchanpur District]]  ([[Mahendranagar, Mahakali|Bhim Dutta]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Mechi Zone]]==\n" +
                "[[Image:Mechi districts.png|thumb|right|250px|Districts of Mechi]]\n" +
                "*[[Ilam District]] ([[Ilam, Nepal|Ilam]])\n" +
                "*[[Jhapa District]] ([[Chandragadhi]])\n" +
                "*[[Panchthar District]] ([[Phidim]])\n" +
                "*[[Taplejung District]] ([[Taplejung]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Narayani Zone]]==\n" +
                "[[Image:Narayani districts.png|thumb|right|250px|Districts of Narayani]]\n" +
                "*[[Bara District]] ([[Kalaiya]])\n" +
                "*[[Chitwan District]]  ([[Bharatpur, Nepal|Bharatpur]])\n" +
                "*[[Makwanpur District]] ([[Hetauda]])\n" +
                "*[[Parsa District]] ([[Birganj]])\n" +
                "*[[Rautahat District]] ([[Gaur, Nepal|Gaur]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Rapti Zone]]==\n" +
                "[[Image:Rapti districts.png|thumb|right|250px|Districts of Rapti]]\n" +
                "*[[Dang Deukhuri District]] ([[Tribuvannagar]])\n" +
                "*[[Pyuthan District]] ([[Pyuthan Khalanga]])\n" +
                "*[[Rolpa District]]  ([[Liwang, Rolpa|Liwang]])\n" +
                "*[[Rukum District]] ([[Musikot]])\n" +
                "*[[Salyan District]] ([[Salyan, Nepal|Salyan Khalanga]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Sagarmatha Zone]]==\n" +
                "[[Image:Sagarmatha districts.png|thumb|right|250px|Districts of Sagarmatha]]\n" +
                "*[[Khotang District]] ([[Diktel]])\n" +
                "*[[Okhaldhunga District]] (Okhaldhunga)\n" +
                "*[[Saptari District]] ([[Rajbiraj]])\n" +
                "*[[Siraha District]] ([[Siraha]])\n" +
                "*[[Solukhumbu District]] ([[Salleri, Solukhumbu|Salleri]])\n" +
                "*[[Udayapur District]] ([[Gaighat]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==[[Seti Zone]]==\n" +
                "[[Image:Seti districts.png|thumb|right|250px|Districts of Seti]]\n" +
                "*[[Achham District]] ([[Mangalsen]])\n" +
                "*[[Bajhang District]] ([[Chainpur, Seti|Chainpur]])\n" +
                "*[[Bajura District]] ([[Martadi]])\n" +
                "*[[Doti District]] ([[Dipayal]])\n" +
                "*[[Kailali District]] ([[Dhangadhi]])\n" +
                "{{Clear}}\n" +
                "\n" +
                "==See also==\n" +
                "*[[Zones of Nepal]]\n" +
                "*[[Regions of Nepal]]\n" +
                "\n" +
                "==External links==\n" +
                "*[http://www.planetnepal.org/wiki/Category:Districts PlanetNepal Districts]\n" +
                "*[http://www.digitalhimalaya.com/collections/nepalmaps/ Collection of Nepalese district maps]\n" +
                "\n" +
                "{{DEFAULTSORT:Districts Of Nepal}}\n" +
                "[[Category:Districts of Nepal| ]]\n" +
                "[[Category:Subdivisions of Nepal]]\n" +
                "[[Category:Lists of country subdivisions|Nepal, Districts]]\n" +
                "[[Category:Country subdivisions of Asia|Nepal 3]]\n" +
                "[[Category:Third-level administrative country subdivisions|Districts, Nepal]]\n" +
                "[[Category:Nepal-related lists]]\n" +
                "\n" +
                "[[da:Distrikter i Nepal]]\n" +
                "[[fr:Districts du Népal]]\n" +
                "[[hi:नेपाल में जिलों की सूची]]\n" +
                "[[it:Distretti del Nepal]]\n" +
                "[[lt:Nepalo rajonai]]\n" +
                "[[nl:Districten van Nepal]]\n" +
                "[[ne:नेपालका जिल्लाहरू]]\n" +
                "[[no:Nepals distrikter]]\n" +
                "[[pt:Distritos do Nepal]]\n" +
                "[[fi:Nepalin piirikunnat]]\n";


        xtractor.extract(wikitext,"article000");
        System.out.println();
    }*/
}
