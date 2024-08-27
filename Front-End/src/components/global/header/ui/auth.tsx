"use client";

import { Image } from "antd";
import Link from "next/link";
import { useEffect, useState } from "react";
import { FaUser } from "react-icons/fa";
import Noiti from "@/components/global/header/ui/modal.noiti";
import { usePathname } from "next/navigation";
import RenderIf from "@/components/global/renderif";
import { useSession } from "next-auth/react";
import { useAppDispatch, useAppSelector } from "@/redux/store";
import { showLoginModal } from "@/redux/login-modal/slice";

export const Auth = () => {
  const [show, setShow] = useState<boolean>(false);
  const hiddenNoiti = () => setShow(false);
  const session = useSession().data;
  const isLogin = session?.user && session.refreshToken;
  const isFirstLoginByProvider = session?.user && !session.refreshToken;
  const user = useAppSelector((state) => state.userInfo.user);
  const path = usePathname();
  const dispatch = useAppDispatch();
  useEffect(() => {
    setShow(false);
  }, [path]);

  return (
    <>
      <RenderIf renderIf={isLogin}>
        <button
          onClick={() => setShow((pre) => !pre)}
          className="flex flex-col text-white items-center px-4 py-1 rounded-lg bg-white bg-opacity-30"
        >
          {user && (
            <Image
              className="rounded-full cursor-pointer"
              width={34}
              height={34}
              alt="avatar"
              src={user?.avatar ? user.avatar : "/images/default/user.svg"}
              fallback="/images/default/user.svg"
              preview={false}
            />
          )}
        </button>
        <Noiti hiddenNoiti={hiddenNoiti} show={show} />
      </RenderIf>
      <RenderIf renderIf={!isLogin && !isFirstLoginByProvider}>
        <Link href="/login">
          <div className="flex gap-1 items-center">
            <FaUser size={28} />
            <p className="text-[12px]">
              Đăng <br /> nhập
            </p>
          </div>
        </Link>
      </RenderIf>
      <RenderIf renderIf={isFirstLoginByProvider}>
        <button
          onClick={() => dispatch(showLoginModal(path))}
          className="flex flex-col text-white items-center px-4 py-1 rounded-lg bg-white bg-opacity-30"
        >
          <Image
            className="rounded-full cursor-pointer"
            width={34}
            height={34}
            alt="avatar"
            src={"/images/default/user.svg"}
            preview={false}
          />
        </button>
      </RenderIf>
    </>
  );
};
