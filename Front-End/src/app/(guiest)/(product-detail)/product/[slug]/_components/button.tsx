"use client";
import { Button } from "@/components/ui/button";
import { openNotification } from "@/lib/nofication";
import { converPriceToVN } from "@/lib/ultils";
import { useAddCartItemMutation } from "@/redux/cart/services";
import { buyNow } from "@/redux/cart/slice";
import { showLoginModal } from "@/redux/login/slice";
import { useAppDispatch } from "@/redux/store";
import { useSession } from "next-auth/react";
import Image from "next/image";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";

const dataColor: {
  color: Color;
  label: string;
  link: string;
}[] = [
  {
    color: "black",
    label: "Đen",
    link: "https://cdn2.cellphones.com.vn/insecure/rs:fill:58:58/q:90/plain/https://cellphones.com.vn/media/catalog/product/g/a/galaxy-a15-den.png",
  },
  {
    color: "yellow",
    label: "Vàng",
    link: "https://cdn2.cellphones.com.vn/insecure/rs:fill:0:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/g/a/galaxy-a15-vang.png",
  },
  {
    color: "blue",
    label: "Xanh",
    link: "https://cdn2.cellphones.com.vn/insecure/rs:fill:0:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/g/a/galaxy-a15-xanh-01.png",
  },
];

type Color = "black" | "yellow" | "blue";
export const SelectColor = ({ price }: { price: number }) => {
  const [picked, setPicked] = useState<Color>("black");
  return (
    <div className="grid grid-cols-3 gap-[10px]">
      {dataColor.map((item) => (
        <Button
          onClick={() => setPicked(item.color)}
          key={item.color}
          variant="outline"
          size="lg"
          className={`flex gap-2 items-center justify-center${
            picked == item.color ? " border-red-600" : ""
          } `}
        >
          <Image src={item.link} alt={item.color} width={25} height={25} />
          <div className="text-[10.5px] text-start">
            <p className="font-bold text-xs ">{item.label}</p>
            <p className="text-xs ">{converPriceToVN(price, "đ")}</p>
          </div>
        </Button>
      ))}
    </div>
  );
};

export const BuyButton = ({ productId }: { productId: number }) => {
  const path = usePathname();
  const session = useSession();
  const user = session.data?.user;
  const route = useRouter();
  const dispatch = useAppDispatch();
  const [addCartItem, { data }] = useAddCartItemMutation();
  if (data) {
    dispatch(buyNow(data));
    route.push("/cart");
  }
  const onclick = () => {
    if (user) {
      addCartItem(productId);
    } else dispatch(showLoginModal(path));
  };
  return (
    <Button
      onClick={onclick}
      className="flex-1 bg-red-600 h-full hover:bg-red-500"
    >
      <div className="text-center">
        <p className="font-bold text-base">Mua ngay</p>
        <p className="text-sm">Giao hàng trong 2h hoặc nhận tại cửa hàng</p>
      </div>
    </Button>
  );
};

export const AddToCartButton = ({ productId }: { productId: number }) => {
  const path = usePathname();
  const session = useSession();
  const user = session.data?.user;
  const [addCartItem, { isLoading }] = useAddCartItemMutation();
  const dispatch = useAppDispatch();
  const onClick = () => {
    if (user) {
      addCartItem(productId);
      openNotification({
        message: "Thêm vào giỏ hàng thành công",
        description: "Thanh toán ngay để nhận ưu đãi",
        notificationType: "success",
      });
    } else dispatch(showLoginModal(path));
  };
  return (
    <Button
      disabled={isLoading}
      onClick={onClick}
      variant="outline"
      className="h-full basis-[60px] px-0 flex-grow-0 flex-shrink-0 border-2 border-red-600 hover:border-red-500"
    >
      <div>
        <Image
          className="mx-auto"
          src="https://cdn2.cellphones.com.vn/insecure/rs:fill:50:0/q:70/plain/https://cellphones.com.vn/media/wysiwyg/add-to-cart.png"
          alt="cart button"
          width={24}
          height={30}
        />
        <p className="text-[8.5px] text-red-600">Thêm vào giỏ</p>
      </div>
    </Button>
  );
};
