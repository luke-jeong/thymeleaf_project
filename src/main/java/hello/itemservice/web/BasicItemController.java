package hello.itemservice.web;


import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import java.util.List;

@Controller
@RequestMapping("/basic/items")
@RequiredArgsConstructor
public class BasicItemController {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "basic/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "basic/item";
    }

    @GetMapping("/add")
    public String addForm() {
        return "basic/addForm";
    }

    //@PostMapping("/add")
    //RequestParam을 사용한 추가 기능 만들기
    public String addItemV1(@RequestParam String itemName,
                       @RequestParam int price,
                       @RequestParam Integer queatity,
                       Model model) {
        Item item = new Item();
        item.setItemName(itemName);
        item.setPrice(price);
        item.setQuantity(queatity);

        itemRepository.save(item);

        model.addAttribute("item", item);
        return "basic/addForm";
    }

    //@PostMapping("/add")
    //ModelAttribute를 이용한 추가기능 만들기
    public String addItemV2(@ModelAttribute("item") Item item,
                            Model model) {
       //ModelAttribute를 사용하면 위의 requestparam을 이용한 api처럼 데이터를 하나하나 추가해줄 필요 없다.
        itemRepository.save(item);
        //model.addAttribute("item", item);
        //model.addAttribute도 자동추가되기 때문에 생략해도 된다.
        return "basic/addForm";
    }
/*위에서 저장과 추가가 /add로 같은 주소를 가지지만 get 과 post로 구분되어 작동한다.*/

    //@PostMapping("/add")
    public String addItemV3(Item item) {
        itemRepository.save(item);
        return "basic/addForm";
        //modelAttribute 자체 생략도 가능하다. 다 생략하고 이렇게 간단하게 표현 가능하다.
    }
    /* ----------------------------------------------*/
    /* 하지만 위와같이 return시 redirect를 쓰지 않고 원래 주소로 돌아가기만 하면, 새로고침 시 (웹 브라우저의 새로고침은 사용자의 마지막 행위를 반복한다.)
    *  다시 post를 날려 id만 다른 같은 내용의 상품을 또 추가하게 된다.
    *
    *  이 문제를 해결하기 위해 redirect를 사용하게 된다. (Post - Redirect - Get, PRG)*/

    //@PostMapping("/add")
    public String addItemV4(Item item) {
        itemRepository.save(item);
        return "redirect:/basic/items/"+item.getId();
    }
    /*-----위 해결책에서 redirect 이후 url에 변수를 직접 더해서 사용하는것은 url 인코딩이 되지 않기 때문에 위험하다.
    이를 해결하기 위해 RedirectAttributes를 사용하면 아래와 같다.------*/

    @PostMapping("/add")
    public String addItemV5(Item item, RedirectAttributes redirectAttributes) {
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/basic/items/{itemId}";
        //redirectAttributes에 입력하지 않은 값들은 queryParam으로 넘어간다
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "/basic/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/basic/items/{itemId}";
        //수정 후 상세화면으로 돌아가기위해 주소 이동이 되어야하므로 리다이렉트를 사용했다.
    }



    /*
     * 테스트용 데이터 추가
     * */
    @PostConstruct
    public void init() {
        itemRepository.save(new Item("itemA", 10000, 10));
        itemRepository.save(new Item("itemB", 20000, 20));

    }
}
