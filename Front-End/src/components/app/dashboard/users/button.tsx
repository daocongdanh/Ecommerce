"use client";

import { Button } from "@/components/ui/button";
import { useState } from "react";
import { CiEdit } from "react-icons/ci";
import RenderIf from "@/components/global/renderif";
import { FaLock, FaUnlock } from "react-icons/fa";
import { message, Tooltip } from "antd";
import { updateStatus } from "@/services/user";
import { AiOutlineLoading } from "react-icons/ai";
import Link from "next/link";

export const LockUser = ({
  user,
  isDemo,
}: {
  user: User;
  isDemo?: boolean;
}) => {
  const isAdmin = user.roles.includes("admin");
  const change = async (preStatus: boolean) => {
    setLoading(true);
    if (isDemo) {
      message.warning("Chỉ được phép xem");
    } else if (isAdmin) {
      message.warning("Không được thay đổi tài khoản admin");
    } else {
      const res = await updateStatus(user.id, !preStatus);
      if (res.isError) message.error(res.message);
      else message.success(res.message);
    }
    setLoading(false);
  };
  const [loading, setLoading] = useState<boolean>(false);
  return (
    <>
      <RenderIf renderIf={user.active}>
        <Tooltip title="Khóa" color="red">
          <Button
            onClick={() => change(true)}
            className="size-6"
            variant="ghost"
            size="icon"
          >
            <RenderIf renderIf={!loading}>
              <FaUnlock size={18} />
            </RenderIf>
            <RenderIf renderIf={loading}>
              <AiOutlineLoading className="mr-2 size-[14px] animate-spin" />
            </RenderIf>
          </Button>
        </Tooltip>
      </RenderIf>
      <RenderIf renderIf={!user.active}>
        <Tooltip title="Mở khóa" color="green">
          <Button
            onClick={() => change(false)}
            className="size-6"
            variant="ghost"
            size="icon"
          >
            <FaLock size={18} />
          </Button>
        </Tooltip>
      </RenderIf>
    </>
  );
};

export const EditUser = ({ userId }: { userId: number }) => {
  return (
    <>
      <Tooltip title="Chỉnh sửa" color="green">
        <Link href={`/dashboard/users/${userId}/edit`}>
          <Button
            className="text-red-600 hover:text-red-500 size-6"
            variant="ghost"
            size="icon"
          >
            <CiEdit size={20} />
          </Button>
        </Link>
      </Tooltip>
    </>
  );
};
export const AddUser = () => {
  return (
    <Link href="/dashboard/users/create">
      <Button>Thêm</Button>
    </Link>
  );
};
