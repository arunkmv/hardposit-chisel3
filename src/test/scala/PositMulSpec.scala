import chisel3.iotesters.{ChiselFlatSpec, PeekPokeTester}
import hardposit.PositMul

class PositMulSpec extends ChiselFlatSpec {

  private class PositMulTest(c: PositMul, num1: Int, num2: Int, expectedPosit: Int, isNaR: Boolean) extends PeekPokeTester(c) {
    poke(c.io.num1, num1)
    poke(c.io.num2, num2)
    step(1)
    expect(c.io.isNaR, isNaR)
    expect(c.io.out, expectedPosit)
  }

  private def test(nbits: Int, es: Int, num1: Int, num2: Int, expectedPosit: Int, isNaR: Boolean = false): Boolean = {
    chisel3.iotesters.Driver(() => new PositMul(nbits, es)) {
      c => new PositMulTest(c, num1, num2, expectedPosit, isNaR)
    }
  }

  it should "return multiplied value signs and exponent signs are positive" in {
    assert(test(16, 1, 0x5A00, 0x6200, 0x7010))
  }

  it should "return multiplied value when signs are different" in {
    assert(test(16, 1, 0xA600, 0x6200, 0x8FF0))
    assert(test(16, 1, 0x6200, 0xA600, 0x8FF0))
  }

  it should "return the zero when one of the number is zero" in {
    assert(test(8, 0, 0x6F, 0x00, 0x00))
    assert(test(8, 0, 0x00, 0x32, 0x00))
  }

  it should "return infinity when one of the number is infinite" in {
    assert(test(8, 1, 0x80, 0x43, 0x80, isNaR = true))
    assert(test(8, 1, 0x43, 0x80, 0x80, isNaR = true))
  }

  it should "return isNan as true when one is zero and another one is infinity" in {
    assert(test(16, 7, 0x8000, 0, 0x8000, isNaR = true))
    assert(test(16, 7, 0, 0x8000, 0x8000, isNaR = true))
  }

  it should "return the positive number when there are two negative numbers multiplied" in {
    assert(test(16, 1, 0xA600, 0x9E00, 0x7010))
  }

  it should "return the correct multiplication when of the number has smallest exponent" in {
    assert(test(8, 2, 0x47, 0x10, 0x14))
  }
}
