"use client";

import { Button } from "antd";
import { FaCartPlus } from "react-icons/fa";

export const AddProductToCart = ({ productId }: { productId: string }) => {
  const onClick = () => {
    console.log(productId);
  };
  return (
    <>
      {/* Notification */}
      <Button onClick={onClick} danger icon={<FaCartPlus />} />
    </>
  );
};
